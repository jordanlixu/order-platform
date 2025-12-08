--changeset order-service:202412041200-create-processed_event-table
CREATE TABLE processor.processed_event
(
    event_id   UUID PRIMARY KEY,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- rollback drop table processor.processed_event;
