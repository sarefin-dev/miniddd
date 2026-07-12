# miniddd — Payment Order Service

A reference Spring Boot microservice demonstrating **Hexagonal Architecture**, **Clean Architecture's
dependency rule**, and **DDD tactical patterns** working together, end to end, in one small codebase.

## The story

A customer places an order, then confirms payment through one of three gateways — **Stripe**, **bKash**, or
**Nagad**. Regardless of which gateway is used, a successful payment always produces the same domain event
(`OrderConfirmed`), published through a transactional outbox to Kafka, so downstream consumers never need to
know which gateway was involved.

## Architecture

```
domain/          Order aggregate, Money/OrderId value objects, OrderStatus/PaymentMethod enums, domain events.
                 No Spring, no JPA, no adapter imports — plain Java only.

application/     CreateOrderUseCase, ConfirmPaymentUseCase. Depend only on ports, never on adapters.
                 Also framework-free (including no @Transactional — see below).

port/            Interfaces only.
  in/              Called by adapters, implemented by use cases (CreateOrderPort, ConfirmPaymentPort).
  out/             Called by use cases, implemented by adapters (OrderRepositoryPort, PaymentGatewayPort,
                   EventPublisherPort, TransactionPort).

adapter/         The only layer allowed to import Spring, JPA, or Kafka.
  in/web/          REST controller.
  out/payment/     StripeAdapter, BkashAdapter.
  out/persistence/ Postgres-backed OrderRepositoryPort implementation.
  out/messaging/   Transactional outbox writer + Kafka relay.
  out/transaction/ Spring transaction management, exposed to the application layer as a plain port.

config/          Composition root — the only place use cases are wired up as Spring beans.
```

Dependencies only point inward: `adapter` → `port` + `domain`, `application` → `port` + `domain`, `port` →
`domain`, `domain` → nothing. See [CLAUDE.md](CLAUDE.md) for the reasoning behind the two non-obvious design
choices (`TransactionPort` and the transactional outbox).

## Getting started

Requires JDK 25 (point `JAVA_HOME` at it if it's not your default JDK). Maven is not required to be installed
— use the included wrapper.

```
./mvnw test        # run the domain test suite (no Spring context needed)
./mvnw package      # build the jar
```

Running the app (`./mvnw spring-boot:run`) additionally requires:
- Postgres reachable at `localhost:5432`, database `miniddd`, user/password `miniddd`/`miniddd`
- Kafka reachable at `localhost:9092`

(No docker-compose is included yet for spinning these up locally.)

## API

**Create an order**

```
POST /api/orders
{
  "customerId": "b3b2c1a0-0000-0000-0000-000000000001",
  "amount": 49.99,
  "currency": "USD"
}
```
→ `201 Created` with `{ "orderId": "..." }`

**Confirm payment**

```
POST /api/orders/{orderId}/confirm-payment
{
  "paymentMethod": "STRIPE",
  "paymentToken": "tok_example"
}
```
→ `202 Accepted`. Confirmation involves a call to an external gateway, so the response doesn't carry a
synchronous success/failure verdict — check the order's status afterward, or consume the `order.confirmed` /
`order.payment-failed` Kafka topics.
