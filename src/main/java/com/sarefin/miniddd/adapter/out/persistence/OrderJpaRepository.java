package com.sarefin.miniddd.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Adapter — Spring Data repository; exists only so OrderRepositoryAdapter has something to delegate to.
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
