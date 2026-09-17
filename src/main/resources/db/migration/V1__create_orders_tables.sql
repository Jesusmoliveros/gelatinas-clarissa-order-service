CREATE TABLE orders
(
    id                    BIGSERIAL PRIMARY KEY,

    order_number          VARCHAR(30)    NOT NULL UNIQUE,

    customer_name         VARCHAR(120)   NOT NULL,
    customer_phone        VARCHAR(20)    NOT NULL,

    contact_source        VARCHAR(20)    NOT NULL,

    status                VARCHAR(30)    NOT NULL DEFAULT 'CONFIRMED',

    requested_delivery_at TIMESTAMP      NOT NULL,

    delivery_type         VARCHAR(30)    NOT NULL,
    delivery_details      VARCHAR(500),

    subtotal              NUMERIC(10, 2) NOT NULL,
    extras_total          NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total                 NUMERIC(10, 2) NOT NULL,

    payment_method        VARCHAR(20),
    payment_status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    paid_at               TIMESTAMP,

    general_notes         VARCHAR(1000),

    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_contact_source
        CHECK (contact_source IN (
                                  'FACEBOOK',
                                  'WHATSAPP',
                                  'PHONE_CALL',
                                  'OTHER'
            )),

    CONSTRAINT chk_order_status
        CHECK (status IN (
                          'CONFIRMED',
                          'IN_PREPARATION',
                          'READY',
                          'OUT_FOR_DELIVERY',
                          'DELIVERED',
                          'CANCELLED'
            )),

    CONSTRAINT chk_order_delivery_type
        CHECK (delivery_type IN (
                                 'MEETING_POINT',
                                 'HOME_DELIVERY'
            )),

    CONSTRAINT chk_order_payment_method
        CHECK (
            payment_method IS NULL
                OR payment_method IN ('CASH', 'TRANSFER')
            ),

    CONSTRAINT chk_order_payment_status
        CHECK (payment_status IN ('PENDING', 'PAID')),

    CONSTRAINT chk_order_amounts
        CHECK (
            subtotal >= 0
                AND extras_total >= 0
                AND total >= 0
            )
);

CREATE TABLE order_items
(
    id                    BIGSERIAL PRIMARY KEY,

    order_id              BIGINT         NOT NULL,

    product_id            BIGINT,
    variant_id            BIGINT,

    product_name          VARCHAR(120)   NOT NULL,
    flavor                VARCHAR(150),
    size                  VARCHAR(80),

    quantity              INTEGER        NOT NULL,
    unit_price            NUMERIC(10, 2) NOT NULL,

    customization_notes   VARCHAR(1000),

    transfer_requested    BOOLEAN        NOT NULL DEFAULT FALSE,
    transfer_instructions VARCHAR(500),
    transfer_image_url    VARCHAR(500),
    transfer_price        NUMERIC(10, 2) NOT NULL DEFAULT 0,

    additional_price      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    line_total            NUMERIC(10, 2) NOT NULL,

    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_order_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_item_amounts
        CHECK (
            unit_price >= 0
                AND transfer_price >= 0
                AND additional_price >= 0
                AND line_total >= 0
            )
);

CREATE INDEX idx_orders_customer_phone
    ON orders (customer_phone);

CREATE INDEX idx_orders_status
    ON orders (status);

CREATE INDEX idx_orders_payment_status
    ON orders (payment_status);

CREATE INDEX idx_orders_delivery_date
    ON orders (requested_delivery_at);

CREATE INDEX idx_orders_created_at
    ON orders (created_at);

CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);