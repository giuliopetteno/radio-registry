CREATE TABLE IF NOT EXISTS organization(
     id SERIAL PRIMARY KEY,
     name TEXT NOT NULL,
     code TEXT NOT NULL,
     description TEXT,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS compartment(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    description TEXT,
    organization_id BIGINT,
    parent_compartment_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_compartment_organization
    FOREIGN KEY (organization_id)
    REFERENCES organization(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_compartment_parent
    FOREIGN KEY (parent_compartment_id)
    REFERENCES compartment(id)
    ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS device_type(
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS device(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    device_type_id INT NOT NULL,
    serial_number TEXT NOT NULL,
    description TEXT,
    installation_date DATE NOT NULL,
    organization_id BIGINT,
    compartment_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_device_organization
    FOREIGN KEY (organization_id)
    REFERENCES organization(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_device_compartment
    FOREIGN KEY (compartment_id)
    REFERENCES compartment(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_device_type
    FOREIGN KEY (device_type_id)
    REFERENCES device_type(id)
    ON DELETE RESTRICT,

    CONSTRAINT chk_device_parent CHECK (
    (organization_id IS NOT NULL AND compartment_id IS NULL)
    OR
    (organization_id IS NULL AND compartment_id IS NOT NULL))
);

CREATE INDEX idx_compartment_organization ON compartment(organization_id);
CREATE INDEX idx_device_organization ON device(organization_id);
CREATE INDEX idx_device_compartment ON device(compartment_id);
CREATE INDEX idx_compartment_parent ON compartment(parent_compartment_id);
