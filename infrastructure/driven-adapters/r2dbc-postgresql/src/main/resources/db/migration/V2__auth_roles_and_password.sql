CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE crediya_users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100) NOT NULL DEFAULT '$2a$10$PLACEHOLDERPLACEHOLDERPLACEHOLDERPLACEHOLD',
    ADD CONSTRAINT users_email_unique UNIQUE (email);

CREATE TABLE IF NOT EXISTS role (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(30) NOT NULL UNIQUE CHECK (name ~ '^[A-Z_]+$')
    );

CREATE TABLE IF NOT EXISTS user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES crediya_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id)  ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON crediya_users(email);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role ON user_role(role_id);

INSERT INTO role (name) VALUES ('ADMIN'), ('ADVISOR'), ('CUSTOMER')
    ON CONFLICT (name) DO NOTHING;

ALTER TABLE crediya_users ALTER COLUMN password_hash DROP DEFAULT;