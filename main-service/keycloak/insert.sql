-- Initial data for the Keycloak database (realm "lms"): roles and users.
-- Runs against Keycloak's OWN database (keycloak-db), not the main-service database,
-- so it is not part of the main-service Liquibase changelog.
-- Idempotent: safe to run several times (ON CONFLICT DO NOTHING).
BEGIN;

-- 1) Realm roles (these become GrantedAuthority in Spring Security)
INSERT INTO keycloak_role (id, client_realm_constraint, client_role, description, name, realm_id, client, realm)
SELECT '73a8e684-1804-4812-9d06-d7d09265a28c', r.id, false, 'Administrator', 'ROLE_ADMIN', r.id, NULL, r.id
FROM realm r WHERE r.name = 'lms'
ON CONFLICT DO NOTHING;

INSERT INTO keycloak_role (id, client_realm_constraint, client_role, description, name, realm_id, client, realm)
SELECT '904e8be2-7890-414b-9d85-b415e2713703', r.id, false, 'Teacher', 'ROLE_TEACHER', r.id, NULL, r.id
FROM realm r WHERE r.name = 'lms'
ON CONFLICT DO NOTHING;

INSERT INTO keycloak_role (id, client_realm_constraint, client_role, description, name, realm_id, client, realm)
SELECT '4407bca2-4170-4b24-b7c5-796303aa9aa8', r.id, false, 'Student / regular user', 'ROLE_USER', r.id, NULL, r.id
FROM realm r WHERE r.name = 'lms'
ON CONFLICT DO NOTHING;

-- 2) Users (password = username, hashed with pbkdf2-sha512, 210000 iterations)
INSERT INTO user_entity (id, email, email_constraint, email_verified, enabled, first_name, last_name, realm_id, username, created_timestamp, not_before)
SELECT 'b5ebe326-9f1a-42ea-ac8c-1c03a936f5d2', 'admin1@lms.local', 'admin1@lms.local', true, true, 'Admin', 'User', r.id, 'admin1', (extract(epoch FROM now()) * 1000)::bigint, 0
FROM realm r WHERE r.name = 'lms'
ON CONFLICT DO NOTHING;

INSERT INTO credential (id, type, user_id, created_date, secret_data, credential_data, priority)
SELECT '75debfc9-f649-4626-add8-f8deb1dcc8f4', 'password', u.id, (extract(epoch FROM now()) * 1000)::bigint,
       '{"value":"Q8mt479rIDjkksP1CQdd/YP1dWAk8Plz/osBdhXBvPLnGPcDNyPNZHkQIFK2sX77onqIELRlTCbVNzX8FPcEQw==","salt":"NbHzEH8UbAbgr1RZ5hpkrA==","additionalParameters":{}}',
       '{"hashIterations":210000,"algorithm":"pbkdf2-sha512","additionalParameters":{}}', 10
FROM user_entity u JOIN realm r ON r.id = u.realm_id
WHERE r.name = 'lms' AND u.username = 'admin1'
  AND NOT EXISTS (SELECT 1 FROM credential c WHERE c.user_id = u.id AND c.type = 'password')
ON CONFLICT DO NOTHING;

-- role mappings for admin1: ROLE_ADMIN + the realm default role
INSERT INTO user_role_mapping (role_id, user_id)
SELECT k.id, u.id
FROM user_entity u
JOIN realm r ON r.id = u.realm_id
JOIN keycloak_role k ON k.realm_id = r.id AND k.name IN ('ROLE_ADMIN', 'default-roles-lms')
WHERE r.name = 'lms' AND u.username = 'admin1'
ON CONFLICT DO NOTHING;

INSERT INTO user_entity (id, email, email_constraint, email_verified, enabled, first_name, last_name, realm_id, username, created_timestamp, not_before)
SELECT '0327d4c2-9c3e-4d12-a3b1-a58fc00e2055', 'teacher1@lms.local', 'teacher1@lms.local', true, true, 'Teacher', 'User', r.id, 'teacher1', (extract(epoch FROM now()) * 1000)::bigint, 0
FROM realm r WHERE r.name = 'lms'
ON CONFLICT DO NOTHING;

INSERT INTO credential (id, type, user_id, created_date, secret_data, credential_data, priority)
SELECT '618be295-813b-444d-943b-31eff63e41bf', 'password', u.id, (extract(epoch FROM now()) * 1000)::bigint,
       '{"value":"1t1i3rCH1K86wd8OGi6lu8xbU6kAwVSZNPM55OzZNQH4ADt060XgEEYIZb3Mff2rmghet5OCuMaqPl6OnzvZJg==","salt":"6jwR78+k/5e8v/VDVMTcbg==","additionalParameters":{}}',
       '{"hashIterations":210000,"algorithm":"pbkdf2-sha512","additionalParameters":{}}', 10
FROM user_entity u JOIN realm r ON r.id = u.realm_id
WHERE r.name = 'lms' AND u.username = 'teacher1'
  AND NOT EXISTS (SELECT 1 FROM credential c WHERE c.user_id = u.id AND c.type = 'password')
ON CONFLICT DO NOTHING;

-- role mappings for teacher1: ROLE_TEACHER + the realm default role
INSERT INTO user_role_mapping (role_id, user_id)
SELECT k.id, u.id
FROM user_entity u
JOIN realm r ON r.id = u.realm_id
JOIN keycloak_role k ON k.realm_id = r.id AND k.name IN ('ROLE_TEACHER', 'default-roles-lms')
WHERE r.name = 'lms' AND u.username = 'teacher1'
ON CONFLICT DO NOTHING;

INSERT INTO user_entity (id, email, email_constraint, email_verified, enabled, first_name, last_name, realm_id, username, created_timestamp, not_before)
SELECT 'ae863f01-8053-4488-ba1b-307b6c739e07', 'user1@lms.local', 'user1@lms.local', true, true, 'User', 'User', r.id, 'user1', (extract(epoch FROM now()) * 1000)::bigint, 0
FROM realm r WHERE r.name = 'lms'
ON CONFLICT DO NOTHING;

INSERT INTO credential (id, type, user_id, created_date, secret_data, credential_data, priority)
SELECT '75248bb8-1729-4df1-8434-05cc63a029e4', 'password', u.id, (extract(epoch FROM now()) * 1000)::bigint,
       '{"value":"wEWD3gMQLUEyrRKIfV7dJSKsjY15xlLh/ayq7NsB/NboSkTqn0+r0w16GOYVVt5TwsQuTgZFYlktVvJXGCiZGg==","salt":"KqN+HuJGdqYEHQ4Q3xPL5Q==","additionalParameters":{}}',
       '{"hashIterations":210000,"algorithm":"pbkdf2-sha512","additionalParameters":{}}', 10
FROM user_entity u JOIN realm r ON r.id = u.realm_id
WHERE r.name = 'lms' AND u.username = 'user1'
  AND NOT EXISTS (SELECT 1 FROM credential c WHERE c.user_id = u.id AND c.type = 'password')
ON CONFLICT DO NOTHING;

-- role mappings for user1: ROLE_USER + the realm default role
INSERT INTO user_role_mapping (role_id, user_id)
SELECT k.id, u.id
FROM user_entity u
JOIN realm r ON r.id = u.realm_id
JOIN keycloak_role k ON k.realm_id = r.id AND k.name IN ('ROLE_USER', 'default-roles-lms')
WHERE r.name = 'lms' AND u.username = 'user1'
ON CONFLICT DO NOTHING;

COMMIT;