
--liquibase formatted sql
--changeset team12:008-ensure-user-id-1 context:dev,local

-- Ensure there is a user with id=1 for local/dev environments
-- Only insert when no user with id=1 exists
INSERT INTO users (id, name, email, created_at)
SELECT 1, 'seeduser', 'seeduser@example.com', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

-- Reset the users id sequence to MAX(id) so future inserts do not conflict
SELECT setval(pg_get_serial_sequence('users','id'), (SELECT COALESCE(MAX(id), 1) FROM users));

--rollback DELETE FROM users WHERE id = 1;
