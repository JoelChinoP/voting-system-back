-- Crear esquema de base de datos PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla para controlar estado de votación de usuarios
CREATE TABLE IF NOT EXISTS user_voting_status (
    user_id UUID PRIMARY KEY,
    election_id UUID NOT NULL DEFAULT '550e8400-e29b-41d4-a716-446655440000',
    has_voted BOOLEAN DEFAULT FALSE,
    voted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de usuarios (simulada para testing)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    password VARCHAR(255),
    has_voted BOOLEAN DEFAULT FALSE,
    is_eligible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de candidatos (para validar candidate_id)
CREATE TABLE IF NOT EXISTS candidates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    party VARCHAR(100),
    election_id UUID NOT NULL DEFAULT '550e8400-e29b-41d4-a716-446655440000',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para almacenar tokens revocados
CREATE TABLE IF NOT EXISTS revoked_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token VARCHAR(512) NOT NULL,
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_user_voting_status_election ON user_voting_status(election_id);
CREATE INDEX IF NOT EXISTS idx_user_voting_status_voted ON user_voting_status(has_voted);
CREATE INDEX IF NOT EXISTS idx_candidates_election ON candidates(election_id);

-- Función para actualizar timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para actualizar updated_at automáticamente
CREATE TRIGGER update_user_voting_status_updated_at 
    BEFORE UPDATE ON user_voting_status 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();