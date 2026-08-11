CREATE TABLE IF NOT EXISTS radio_registry.users(
   id BIGSERIAL PRIMARY KEY,
   username TEXT NOT NULL UNIQUE,
   email TEXT NOT NULL UNIQUE,
   password TEXT NOT NULL,
   enabled BOOLEAN NOT NULL DEFAULT TRUE,
   account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS radio_registry.roles(
   id BIGSERIAL PRIMARY KEY,
   name TEXT NOT NULL UNIQUE,
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS radio_registry.users_roles(
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_users_roles_role_id ON radio_registry.users_roles(role_id);

INSERT INTO radio_registry.roles(name) VALUES ('ADMIN');

CREATE TABLE IF NOT EXISTS radio_registry.refresh_token(
    id BIGSERIAL PRIMARY KEY,
    token_hash TEXT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    replaced_by_token_id BIGINT,
    issued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_refresh_token_user
    FOREIGN KEY (user_id)
    REFERENCES radio_registry.users(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_refresh_token_replaced_by
    FOREIGN KEY (replaced_by_token_id)
    REFERENCES radio_registry.refresh_token(id)
    ON DELETE SET NULL
);

CREATE INDEX idx_refresh_token_user ON radio_registry.refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires ON radio_registry.refresh_token(expires_at) WHERE revoked = FALSE;