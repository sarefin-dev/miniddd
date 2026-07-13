# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A reference Spring Boot microservice — a "Payment Order Service" — built to demonstrate Hexagonal Architecture,
Clean Architecture's dependency rule, and DDD tactical patterns working together for someone learning these
concepts. It is not optimized for shipping speed; it is optimized for the boundaries being easy to see and
easy to point at.

Domain story: a customer places an order, then confirms payment through one of three gateways (Stripe, bKash,
Nagad). All three converge on the same domain event (`OrderConfirmed`), published through a transactional
outbox to Kafka, so downstream consumers stay payment-method agnostic.

## Commands

Requires JDK 25. Use the Maven Wrapper (`mvnw`/`mvnw.cmd`) — Maven is not required to be installed globally.

```
./mvnw test                    # run all tests
./mvnw test -Dtest=OrderTest   # run a single test class
./mvnw compile                 # compile only
./mvnw package                 # build the jar (skips nothing; runs tests)
./mvnw package -DskipTests     # build without running tests
./mvnw spring-boot:run          # run the app (needs Postgres on 5432 and Kafka on 29092, see below)
```

The project targets `java.version 25` (see `pom.xml`). This requires Spring Boot **3.5.3+** — Spring Boot
3.3.x cannot run on JDK 25 at all: Spring Framework's internally repackaged ASM copy (used for classpath
component scanning) throws `Incompatible class format` on class file major version 69 (Java 25's bytecode
version). 3.5.3's bundled Spring Framework 6.2.8 reads it fine. If you ever see that error, the parent POM
version has drifted back down — bump it, don't work around it.

On Windows, if Surefire fails with `IllegalArgumentException: 'other' has different root`, it means `%TEMP%`
and the project checkout are on different drive letters — Surefire's classpath-jar mechanism can't relativize
paths across drive roots. Point `TEMP`/`TMP` at a folder on the same drive as the project for the Maven
invocation and re-run.

Local infra the app expects at runtime (see `src/main/resources/application.yml`): Postgres at
`localhost:5432/miniddd` (user/password `miniddd`/`miniddd`) and Kafka at `localhost:29092`. `docker compose up
-d` (repo root) provisions both, including auto-creating the `miniddd` Postgres role/database on first boot —
no manual setup step. All four values (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`) can
be overridden via environment variables if you're pointing at different infra instead.

Kafka in `docker-compose.yml` runs `apache/kafka:latest` in KRaft mode (no Zookeeper) with two listeners:
`PLAINTEXT` for other containers on the same Docker network (`kafka:9092`) and `EXTERNAL` for host access
(`localhost:29092`) — the split matters because a listener advertised only as `kafka:9092` doesn't resolve
outside the Docker network. Its healthcheck calls `/opt/kafka/bin/kafka-broker-api-versions.sh` by full path
deliberately: that image doesn't put `/opt/kafka/bin` on `PATH`, so the bare script name silently fails the
healthcheck (container still works — `depends_on: condition: service_healthy` just never turns true) if you
copy this env block elsewhere and simplify the healthcheck without noticing.

## Architecture: the dependency rule

This is the entire point of the codebase, enforced by directory structure under
`src/main/java/com/sarefin/miniddd/`:

```
domain/          → imports nothing else in this project. No Spring, no JPA, no adapter classes.
application/     → imports domain + port only. No Spring, no JPA, no adapter classes.
port/            → imports domain only. Pure interfaces (in/) and the DTOs their methods need (out/).
adapter/         → imports port + domain. The ONLY layer allowed to import Spring/JPA/Kafka.
config/          → the composition root. Wires application-layer classes into Spring beans.
```

Dependencies only point inward. `adapter` depends on `port` and `domain`; nothing in `domain` or `application`
ever depends on `adapter`. If you're about to add an import from `domain` or `application` to Spring, `jakarta.*`,
or anything under `adapter`, stop — that import belongs in an adapter, reached through a port instead.

### Why `application/` has zero Spring imports, including no `@Transactional`

Most "hexagonal architecture in Spring" reference projects carve out an exception and put `@Transactional`
directly on application services. This project doesn't take that shortcut. Instead:

- `ConfirmPaymentUseCase` depends on `port.out.TransactionPort`, a plain interface with one method:
  `runInTransaction(Runnable work)`.
- `adapter/out/transaction/SpringTransactionAdapter` is the only class that knows Spring manages transactions
  (it wraps `PlatformTransactionManager`/`TransactionTemplate`).

This is the one deliberate deviation from "just do what Buckpal does" — worth knowing if you've seen that
style before and wonder why `@Transactional` is missing from the use case.

### Multi-gateway routing without a new abstraction

`PaymentGatewayPort` (one interface) has two implementations wired into a Spring bean: `StripeAdapter` and
`BkashAdapter` (Nagad is named in the domain enum `PaymentMethod` but intentionally has no adapter yet).
`ConfirmPaymentUseCase` receives `List<PaymentGatewayPort>` and picks the one whose `supports(PaymentMethod)`
returns true. No routing/registry port was introduced — the port itself carries the routing predicate.

### The transactional outbox

`EventPublisherPort.publish(event)` does not call Kafka directly. `OutboxEventPublisherAdapter` writes the
event to an `outbox_event` table row in the same transaction as the order save (both happen inside
`ConfirmPaymentUseCase`'s `transactionPort.runInTransaction(...)` block). A separate component,
`OutboxRelay` (`@Scheduled`, polls every `miniddd.outbox.polling-interval-ms`, default 2000ms), reads unpublished
rows and sends them to Kafka (`order.confirmed` for `OrderConfirmed`, `order.payment-failed` for `PaymentFailed`).
Each row is sent and marked published individually: `KafkaTemplate.send()` returns a future, so the relay blocks
on it (`.get(5, TimeUnit.SECONDS)`) before saving `published = true` — if that were skipped, a transaction could
commit the "published" flag before the send actually reached the broker, permanently losing the event on a
Kafka outage. On a send failure the row is left `published = false` and retried on the next poll.

This per-event design is also why `OutboxEventEntity.payload` is a plain `TEXT` column and deliberately not
`@Lob`: `findByPublishedFalseOrderByOccurredOnAsc()` returns from its own (short) read transaction before
`getPayload()` is ever called in the loop, and Postgres large objects can only be streamed inside the
transaction that read them — reading one afterward throws `Large Objects may not be used in auto-commit
mode`. If you're tempted to add `@Lob` back for a "properly typed" large payload, don't, without first moving
the payload read back inside a transaction.

This is why `ConfirmPaymentUseCase.confirmPayment()` calls the payment gateway *before* opening the
transaction: an external HTTP call to Stripe/bKash must never happen while a DB transaction is held open.

### Package map

| Package | Layer | Notes |
|---|---|---|
| `domain.order` | Domain | `Order` aggregate, `Money`/`OrderId` value objects, `OrderStatus`/`PaymentMethod` enums |
| `domain.order.event` | Domain | `DomainEvent`, `OrderConfirmed`, `PaymentFailed` |
| `application.order` | Application | `CreateOrderUseCase`, `ConfirmPaymentUseCase`, `GetOrderUseCase` |
| `port.in` | Port | `CreateOrderPort`, `ConfirmPaymentPort`, `GetOrderPort` — driven side, adapters call these |
| `port.out` | Port | `OrderRepositoryPort`, `PaymentGatewayPort`, `EventPublisherPort`, `TransactionPort` — driving side, adapters implement these |
| `adapter.in.web` | Adapter | `OrderController`, `GlobalExceptionHandler` — depend only on `port.in` (and, for the handler, on the exception types themselves) |
| `adapter.out.payment.{stripe,bkash}` | Adapter | Gateway adapters |
| `adapter.out.persistence` | Adapter | JPA entity + Spring Data repo + `OrderRepositoryAdapter` (domain↔entity mapping) |
| `adapter.out.messaging` | Adapter | Outbox entity/repo, `OutboxEventPublisherAdapter`, `OutboxRelay` |
| `adapter.out.transaction` | Adapter | `SpringTransactionAdapter` |
| `config` | Composition root | `UseCaseConfiguration` — the only place use cases become Spring `@Bean`s |

### Testing philosophy

`OrderTest` (`src/test/java/.../domain/order/OrderTest.java`) instantiates `Order` directly with no Spring
context and no mocks. If a future domain test ever needs `@SpringBootTest`, a mock, or an import from
`adapter`, that's a signal the domain layer has picked up a dependency it shouldn't have.
