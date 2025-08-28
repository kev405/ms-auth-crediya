-- Habilita una sola vez (si no lo has hecho):
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- o: CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE crediya_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- o uuid_generate_v4()
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    address TEXT NOT NULL,
    birth_date DATE NOT NULL,
    phone TEXT NOT NULL,
    salary NUMERIC(15,2) NOT NULL
);