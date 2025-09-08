
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE crediya_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    address TEXT NOT NULL,
    birth_date DATE NOT NULL,
    phone TEXT NOT NULL,
    salary NUMERIC(15,2) NOT NULL
);