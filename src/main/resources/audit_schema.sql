CREATE TABLE IF NOT EXISTS audit_logs(
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100),
    user_roles JSONB,
    ip_address VARCHAR(45),
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(50),
    description VARCHAR(200),
    success BOOLEAN NOT NULL,
    error_detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_username ON audit_logs(username, created_at DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id, created_at DESC);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type, created_at DESC);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_failures ON audit_logs(created_at DESC) WHERE success = FALSE;