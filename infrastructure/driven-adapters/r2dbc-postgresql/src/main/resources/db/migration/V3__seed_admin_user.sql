-- V3__seed_admin_user.sql
-- Crea (o asegura) un usuario administrador y le asigna el rol ADMIN

WITH upsert_user AS (
INSERT INTO crediya_users (
    name, last_name, email, address, birth_date, phone, salary, password_hash
)
VALUES (
    'Admin', 'System', 'karistizabal307@gmail.com',
    'Calle 32', DATE '1990-01-01', '+573173179762', 10000000,
    crypt('ChangeMe_2025', gen_salt('bf'))
    )
ON CONFLICT (email) DO UPDATE
                           SET name         = EXCLUDED.name,
                           last_name    = EXCLUDED.last_name,
                           address      = EXCLUDED.address,
                           birth_date   = EXCLUDED.birth_date,
                           phone        = EXCLUDED.phone,
                           salary       = EXCLUDED.salary,
                           password_hash = EXCLUDED.password_hash
                           RETURNING id
                           ),
                           sel_user AS (
                       SELECT id FROM upsert_user
                       UNION
                       SELECT id FROM crediya_users WHERE email = 'karistizabal307@gmail.com'
                           ),
                           sel_role AS (
                       SELECT id FROM role WHERE name = 'ADMIN'
                           )
                       INSERT INTO user_role (user_id, role_id)
SELECT su.id, sr.id
FROM sel_user su
         CROSS JOIN sel_role sr
    ON CONFLICT (user_id, role_id) DO NOTHING;