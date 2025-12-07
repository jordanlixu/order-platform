package org.example.order.order.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;


import java.time.OffsetDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to aggregate root
    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    // Business event type
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    // Event payload stored as JSON
    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb",nullable = false)
    private JsonNode payload;

    // Status enum corresponding to PostgreSQL ENUM
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    // Creation timestamp, database DEFAULT NOW() ensures fallback
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Processed timestamp
    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    // Optional retry count
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

}
