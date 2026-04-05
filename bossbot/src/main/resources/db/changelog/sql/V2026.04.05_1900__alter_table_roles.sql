--liquibase formatted sql
--changeset team12:005-fix-role-name-type

ALTER TABLE roles ALTER COLUMN role_name TYPE VARCHAR(50);

--rollback ALTER TABLE roles ALTER COLUMN role_name TYPE smallint;
