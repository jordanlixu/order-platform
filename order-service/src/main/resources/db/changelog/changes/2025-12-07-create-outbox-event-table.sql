--changeset order-service:202412071310-create-outbox-table

-- Create status enum type first
CREATE TYPE orders.outbox_status AS ENUM ('NEW', 'PROCESSING', 'DONE', 'FAILED');

-- Create Outbox table
CREATE TABLE orders.outbox_event
(
    -- Primary key using modern identity column
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Reference to the order aggregate root
    aggregate_id BIGINT NOT NULL,

    -- Business event type (e.g. OrderCreated, OrderPaid)
    event_type VARCHAR(100) NOT NULL,

    -- Event payload stored as JSONB for efficient querying
    payload JSONB NOT NULL,

    -- Processing status of the event
    status orders.outbox_status NOT NULL DEFAULT 'NEW',

    -- Retry count for failed message retries
    retry_count INTEGER NOT NULL DEFAULT 0,

    -- Timestamp when the event was created
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Timestamp when the event is successfully processed
    processed_at TIMESTAMP WITH TIME ZONE,

    -- Timestamp tracks when batch processing started, used for retrying stuck/expired processing
    processing_started_at TIMESTAMP WITH TIME ZONE
);

-- Important indexes for efficient querying
CREATE INDEX idx_outbox_event_created_at ON orders.outbox_event (created_at);
CREATE INDEX idx_outbox_event_aggregate_id ON orders.outbox_event (aggregate_id);
CREATE INDEX idx_outbox_event_status_created_at ON orders.outbox_event (status, created_at);

--rollback DROP TYPE orders.outbox_status;
--rollback DROP TABLE orders.outbox_event;
