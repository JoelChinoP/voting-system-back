DELETE FROM revoked_tokens;
DELETE FROM user_voting_status;
DELETE FROM candidates WHERE election_id = '550e8400-e29b-41d4-a716-446655440000';
DELETE FROM user_roles;
DELETE FROM users;

-- Insertar usuarios sin roles
INSERT INTO users (id, username, email, full_name, password, has_voted, is_eligible, is_active)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'alexisBltz', 'alexis@voting.com',  'Alexis Bltz',    '$2a$12$...', FALSE, TRUE,  TRUE),
  ('22222222-2222-2222-2222-222222222222', 'user1',      'user1@voting.com',   'Usuario Uno',    '$2a$12$...', FALSE, TRUE,  TRUE),
  ('33333333-3333-3333-3333-333333333333', 'user2',      'user2@voting.com',   'Usuario Dos',    '$2a$12$...', FALSE, TRUE,  TRUE),
  ('44444444-4444-4444-4444-444444444444', 'user3',      'user3@voting.com',   'Usuario Tres',   '$2a$12$...', FALSE, FALSE, TRUE),
  ('55555555-5555-5555-5555-555555555555', 'admin',      'admin@voting.com',   'Administrador',  '$2a$12$...', FALSE, TRUE,  TRUE),
  ('66666666-6666-6666-6666-666666666666', 'testuser',   'test@example.com',   'Usuario Test',   '$2a$12$...', TRUE,  TRUE,  TRUE),
  ('77777777-7777-7777-7777-777777777777', 'inactive',   'inactive@example.com','Usuario Inac',   '$2a$12$...', FALSE, TRUE, FALSE)
ON CONFLICT (id) DO UPDATE
  SET email      = EXCLUDED.email,
      full_name  = EXCLUDED.full_name,
      password   = EXCLUDED.password,
      has_voted  = EXCLUDED.has_voted,
      is_eligible= EXCLUDED.is_eligible,
      is_active  = EXCLUDED.is_active,
      updated_at = CURRENT_TIMESTAMP;

-- Insertar roles en user_roles
INSERT INTO user_roles (user_id, role)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'USER'),
  ('22222222-2222-2222-2222-222222222222', 'USER'),
  ('33333333-3333-3333-3333-333333333333', 'USER'),
  ('44444444-4444-4444-4444-444444444444', 'USER'),
  ('55555555-5555-5555-5555-555555555555', 'ADMIN'),
  ('66666666-6666-6666-6666-666666666666', 'USER'),
  ('77777777-7777-7777-7777-777777777777', 'USER')
ON CONFLICT DO NOTHING;

-- Insertar candidatos
INSERT INTO candidates (id, name, party, election_id, is_active)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa','Candidato A','Partido Azul','550e8400-e29b-41d4-a716-446655440000',TRUE),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb','Candidato B','Partido Rojo','550e8400-e29b-41d4-a716-446655440000',TRUE),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc','Candidato C','Partido Verde','550e8400-e29b-41d4-a716-446655440000',TRUE),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd','Candidato D','Partido Amarillo','550e8400-e29b-41d4-a716-446655440000',FALSE)
ON CONFLICT (id) DO UPDATE
  SET name      = EXCLUDED.name,
      party     = EXCLUDED.party,
      is_active = EXCLUDED.is_active;

-- Insertar estado de votación
INSERT INTO user_voting_status (user_id, election_id, has_voted, voted_at)
VALUES
  ('11111111-1111-1111-1111-111111111111','550e8400-e29b-41d4-a716-446655440000',FALSE,NULL),
  ('22222222-2222-2222-2222-222222222222','550e8400-e29b-41d4-a716-446655440000',FALSE,NULL),
  ('33333333-3333-3333-3333-333333333333','550e8400-e29b-41d4-a716-446655440000',FALSE,NULL),
  ('44444444-4444-4444-4444-444444444444','550e8400-e29b-41d4-a716-446655440000',FALSE,NULL),
  ('55555555-5555-5555-5555-555555555555','550e8400-e29b-41d4-a716-446655440000',FALSE,NULL),
  ('66666666-6666-6666-6666-666666666666','550e8400-e29b-41d4-a716-446655440000',TRUE,CURRENT_TIMESTAMP - INTERVAL '1 day'),
  ('77777777-7777-7777-7777-777777777777','550e8400-e29b-41d4-a716-446655440000',FALSE,NULL)
ON CONFLICT (user_id) DO UPDATE
  SET has_voted  = EXCLUDED.has_voted,
      voted_at   = EXCLUDED.voted_at,
      updated_at = CURRENT_TIMESTAMP;

-- Resumen de datos
DO $$
DECLARE
    usr INTEGER;
    cnd INTEGER;
    vs  INTEGER;
    exists_elec BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO usr FROM users;
    SELECT COUNT(*) INTO cnd FROM candidates WHERE is_active;
    SELECT COUNT(*) INTO vs  FROM user_voting_status;
    SELECT EXISTS(SELECT 1 FROM elections WHERE id='550e8400-e29b-41d4-a716-446655440000')
      INTO exists_elec;

    RAISE NOTICE '';
    RAISE NOTICE '🎯 PRUEBAS DE DATOS:';
    RAISE NOTICE '  Usuarios: %', usr;
    RAISE NOTICE '  Candidatos activos: %', cnd;
    RAISE NOTICE '  Estados de votación: %', vs;
    RAISE NOTICE '  Elección por defecto existe: %', exists_elec;
END $$;