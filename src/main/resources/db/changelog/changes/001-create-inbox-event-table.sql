--liquibase formatted sql

--changeset tevsvet:001-create-inbox-event-table
CREATE TABLE inbox_event (
    id UUID PRIMARY KEY NOT NULL,
    task_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    source_bucket VARCHAR(255) NOT NULL,
    source_object_key VARCHAR(1024) NOT NULL,
    payload TEXT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    available_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset tevsvet:001-inbox-task-id-uniq
CREATE UNIQUE INDEX idx_inbox_event_task_id
    ON inbox_event (task_id);
