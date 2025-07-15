-- Datos de prueba para desarrollo
INSERT INTO users (id, username, email, full_name, password, has_voted, is_eligible) VALUES
('11111111-1111-1111-1111-111111111111', 'alexisBltz', 'alexis@voting.com', 'Alexis Bltz', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', FALSE, TRUE),
('22222222-2222-2222-2222-222222222222', 'user1', 'user1@voting.com', 'Usuario Uno', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', FALSE, TRUE),
('33333333-3333-3333-3333-333333333333', 'user2', 'user2@voting.com', 'Usuario Dos', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', FALSE, TRUE),
('44444444-4444-4444-4444-444444444444', 'user3', 'user3@voting.com', 'Usuario Tres', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', FALSE, FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidates (id, name, party, election_id, is_active) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Candidato A', 'Partido Azul', '550e8400-e29b-41d4-a716-446655440000', TRUE),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Candidato B', 'Partido Rojo', '550e8400-e29b-41d4-a716-446655440000', TRUE),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Candidato C', 'Partido Verde', '550e8400-e29b-41d4-a716-446655440000', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_voting_status (user_id, election_id, has_voted) VALUES
('11111111-1111-1111-1111-111111111111', '550e8400-e29b-41d4-a716-446655440000', FALSE),
('22222222-2222-2222-2222-222222222222', '550e8400-e29b-41d4-a716-446655440000', FALSE),
('33333333-3333-3333-3333-333333333333', '550e8400-e29b-41d4-a716-446655440000', FALSE),
('44444444-4444-4444-4444-444444444444', '550e8400-e29b-41d4-a716-446655440000', FALSE)
ON CONFLICT (user_id) DO NOTHING;