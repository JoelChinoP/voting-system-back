-- Crear esquema de base de datos PostgreSQL para Sistema de Votación
-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Eliminar todo para reinicio limpio
DROP TABLE IF EXISTS revoked_tokens CASCADE;
DROP TABLE IF EXISTS user_voting_status CASCADE;
DROP TABLE IF EXISTS candidates CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS elections CASCADE;

-- Tabla de elecciones (creo primero para integridad referencial)
CREATE TABLE elections (
    id UUID PRIMARY KEY DEFAULT '550e8400-e29b-41d4-a716-446655440000',
    name VARCHAR(255) NOT NULL DEFAULT 'Elección General 2024',
    description TEXT,
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 days'),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de usuarios
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    has_voted BOOLEAN DEFAULT FALSE,
    is_eligible BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    roles VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de estado de votación
CREATE TABLE user_voting_status (
    user_id UUID PRIMARY KEY,
    election_id UUID NOT NULL DEFAULT '550e8400-e29b-41d4-a716-446655440000',
    has_voted BOOLEAN DEFAULT FALSE,
    voted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (election_id) REFERENCES elections(id) ON DELETE CASCADE
);

-- Tabla de candidatos
CREATE TABLE candidates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    party VARCHAR(100),
    election_id UUID NOT NULL DEFAULT '550e8400-e29b-41d4-a716-446655440000',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (election_id) REFERENCES elections(id) ON DELETE CASCADE
);

-- Tabla de tokens revocados
CREATE TABLE revoked_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token_hash VARCHAR(256) NOT NULL UNIQUE,
    user_id UUID,
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    reason VARCHAR(100) DEFAULT 'LOGOUT',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Índices
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_user_voting_status_election ON user_voting_status(election_id);
CREATE INDEX idx_user_voting_status_voted ON user_voting_status(has_voted);
CREATE INDEX idx_candidates_active ON candidates(is_active);
CREATE INDEX idx_revoked_tokens_hash ON revoked_tokens(token_hash);
CREATE INDEX idx_revoked_tokens_expires ON revoked_tokens(expires_at);

-- Función para actualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_voting_status_updated_at
    BEFORE UPDATE ON user_voting_status
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Función para limpiar tokens expirados
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM revoked_tokens
    WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Función para chequear token revocado
CREATE OR REPLACE FUNCTION is_token_revoked(token_hash_param VARCHAR(256))
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM revoked_tokens
        WHERE token_hash = token_hash_param
          AND expires_at > CURRENT_TIMESTAMP
    );
END;
$$ LANGUAGE plpgsql;

-- Insertar elección por defecto
INSERT INTO elections (id, name, description, is_active)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Elección General 2024',
    'Elección presidencial y legislativa',
    TRUE
) ON CONFLICT (id) DO NOTHING;

-- Notificaciones
DO $$
BEGIN
    RAISE NOTICE '✅ ESQUEMA INICIALIZADO';
END $$;


-- Tabla users (sin columna roles)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    password TEXT NOT NULL,
    has_voted BOOLEAN DEFAULT FALSE,
    is_eligible BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tabla auxiliar para roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role    TEXT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índice para acelerar lecturas de roles
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);