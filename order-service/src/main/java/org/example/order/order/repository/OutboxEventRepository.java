package org.example.order.order.repository;

import org.example.order.order.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {


    @Modifying
    @Query(value = """
    WITH cte AS (
        SELECT id
        FROM orders.outbox_event
        WHERE status = 'NEW'
           OR (status = 'PROCESSING' AND processing_started_at < now() - interval '5 second')
           OR status = 'FAILED'
        ORDER BY created_at
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
    )
    UPDATE orders.outbox_event o
    SET status = 'PROCESSING',
        processing_started_at = now()
    WHERE o.id IN (SELECT id FROM cte)
    RETURNING *
    """, nativeQuery = true)
    List<OutboxEvent> fetchAndMarkProcessingBatch(@Param("batchSize") int batchSize);


    @Modifying
    @Transactional
    @Query(value = """
            UPDATE orders.outbox_event
            SET status = 'FAILED'
            WHERE status = 'PROCESSING'
              AND processing_started_at < now() - interval '5 second'
            """, nativeQuery = true)
    void markProcessingTimeoutAsFailed();

}

