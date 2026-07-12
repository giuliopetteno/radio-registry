CREATE TYPE outbox_event_status AS ENUM ('PENDING', 'PROCESSED', 'FAILED', 'DEAD_LETTER');

CREATE TABLE IF NOT EXISTS outbox_event(
    id BIGSERIAL PRIMARY KEY,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    outbox_event_status outbox_event_status NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_event_status_created ON outbox_event(outbox_event_status, created_at) WHERE outbox_event_status = 'PENDING';