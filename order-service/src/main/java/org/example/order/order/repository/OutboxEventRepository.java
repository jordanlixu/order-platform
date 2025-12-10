package org.example.order.order.repository;

import org.example.order.order.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
    SELECT *
    FROM orders.outbox_event
    WHERE status = 'NEW'
    ORDER BY created_at
    LIMIT :batchSize
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<OutboxEvent> fetchNewEvents(@Param("batchSize") int batchSize);

}

