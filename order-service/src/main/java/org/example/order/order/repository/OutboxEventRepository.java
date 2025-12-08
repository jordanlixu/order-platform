package org.example.order.order.repository;

import org.example.order.order.model.OutboxEvent;
import org.example.order.order.model.OutboxStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // find for inspection (not used in atomic fetch)
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAt(OutboxStatus status);

    // Atomic fetch-and-mark-processing (Postgres-specific, native query).
    @Transactional
    @Modifying
    @Query(value = """
        WITH cte AS (
          SELECT id FROM orders.outbox_event
          WHERE status = 'NEW'
          ORDER BY created_at
          LIMIT :batchSize
          FOR UPDATE SKIP LOCKED
        )
        UPDATE orders.outbox_event o
        SET status = 'PROCESSING'
        FROM cte
        WHERE o.id = cte.id
        RETURNING o.*;
        """, nativeQuery = true)
    List<OutboxEvent> fetchAndMarkProcessing(@Param("batchSize") int batchSize);

}

