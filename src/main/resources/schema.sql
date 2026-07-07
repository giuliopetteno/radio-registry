CREATE TABLE IF NOT EXISTS organization(
     id SERIAL PRIMARY KEY,
     name TEXT NOT NULL,
     code TEXT NOT NULL,
     description TEXT,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS department(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    description TEXT,
    organization_id BIGINT,
    parent_department_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_department_organization
    FOREIGN KEY (organization_id)
    REFERENCES organization(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_department_parent
    FOREIGN KEY (parent_department_id)
    REFERENCES department(id)
    ON DELETE SET NULL,

    CONSTRAINT chk_department_parent_structure CHECK(
    (organization_id IS NOT NULL AND parent_department_id IS NULL)
    OR
    (organization_id IS NULL AND parent_department_id IS NOT NULL))
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
    department_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_device_organization
    FOREIGN KEY (organization_id)
    REFERENCES organization(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_device_department
    FOREIGN KEY (department_id)
    REFERENCES department(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_device_type
    FOREIGN KEY (device_type_id)
    REFERENCES device_type(id)
    ON DELETE RESTRICT,

    CONSTRAINT chk_device_parent_structure CHECK(
    (organization_id IS NOT NULL AND department_id IS NULL)
    OR
    (organization_id IS NULL AND department_id IS NOT NULL))
);

CREATE INDEX idx_department_organization ON department(organization_id);
CREATE INDEX idx_device_organization ON device(organization_id);
CREATE INDEX idx_device_department ON device(department_id);
CREATE INDEX idx_department_parent ON department(parent_department_id);
