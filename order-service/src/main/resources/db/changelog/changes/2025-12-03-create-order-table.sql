--changeset order-service:202412031200-create-orders-table
CREATE TABLE orders.orders
(
    id         BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    VARCHAR(100) NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    amount     INTEGER      NOT NULL,
    status     VARCHAR(30)  NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- rollback drop table orders.orders;
