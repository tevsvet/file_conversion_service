--liquibase formatted sql

--changeset tevsvet:001
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY NOT NULL,
    aggregate_type VARCHAR(128) NOT NULL,
    aggregate_id UUID NOT NULL,
    dedup_key VARCHAR(255) NOT NULL,
    partition_key VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    published_at TIMESTAMP,
    available_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset tevsvet:001-index-dedup-key
CREATE UNIQUE INDEX idx_outbox_event_dedup_key
    ON outbox_event (dedup_key);

--changeset tevsvet:001-index-status-available-at
CREATE INDEX idx_outbox_event_status_available_at
    ON outbox_event (status, available_at);
