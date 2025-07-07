-- Crear keyspace para el sistema de votación
CREATE KEYSPACE IF NOT EXISTS voting_system
WITH REPLICATION = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

USE voting_system;

-- Tabla principal para almacenar votos (anónimos)
CREATE TABLE IF NOT EXISTS votes (
    vote_id UUID PRIMARY KEY,
    candidate_id UUID,
    election_id UUID,
    voted_at TIMESTAMP,
    vote_hash TEXT,
    metadata TEXT
);

-- Tabla para conteo rápido por candidato
CREATE TABLE IF NOT EXISTS votes_by_candidate (
    candidate_id UUID,
    election_id UUID,
    vote_id UUID,
    voted_at TIMESTAMP,
    PRIMARY KEY (candidate_id, election_id, vote_id)
) WITH CLUSTERING ORDER BY (election_id ASC, vote_id ASC);

-- Tabla para conteo por tiempo (analytics)
CREATE TABLE IF NOT EXISTS votes_by_time (
    election_id UUID,
    vote_hour TIMESTAMP,
    vote_id UUID,
    candidate_id UUID,
    PRIMARY KEY (election_id, vote_hour, vote_id)
) WITH CLUSTERING ORDER BY (vote_hour DESC, vote_id ASC);

-- Tabla para verificar duplicados (backup de PostgreSQL)
CREATE TABLE IF NOT EXISTS user_votes_log (
    user_id UUID,
    election_id UUID,
    vote_id UUID,
    candidate_id UUID,
    voted_at TIMESTAMP,
    PRIMARY KEY (user_id, election_id)
);
