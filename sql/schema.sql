-- ============================================================
--  Neuro-SQL Schema
--  Run this once against your PostgreSQL database:
--    psql -U postgres -d neurosql -f sql/schema.sql
-- ============================================================

-- Drop in reverse dependency order (safe re-run)
DROP TABLE IF EXISTS eeg_signals CASCADE;
DROP TABLE IF EXISTS sessions    CASCADE;
DROP TABLE IF EXISTS patients    CASCADE;

-- Patients
CREATE TABLE patients (
    patient_id         SERIAL PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    age                INT          NOT NULL CHECK (age > 0),
    history_risk_level VARCHAR(10)  NOT NULL
                           DEFAULT 'LOW'
                           CHECK (history_risk_level IN ('LOW', 'MEDIUM', 'HIGH'))
);

-- Sessions
CREATE TABLE sessions (
    session_id   SERIAL PRIMARY KEY,
    patient_id   INT NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    session_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- EEG Signals (the large time-series table)
CREATE TABLE eeg_signals (
    signal_id    BIGSERIAL PRIMARY KEY,
    session_id   INT     NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    ts_offset    FLOAT   NOT NULL,        -- seconds from session start
    channel_name VARCHAR(10) NOT NULL,    -- Fp1, Fp2, T3, T4, C3, C4, O1, O2
    voltage      FLOAT   NOT NULL         -- microvolts (µV)
);

-- Composite index: critical for Window Function query performance
CREATE INDEX idx_eeg_lookup
    ON eeg_signals (session_id, channel_name, ts_offset);

-- Seed data (sample patients)
INSERT INTO patients (name, age, history_risk_level) VALUES
    ('Arjun Sharma',   34, 'HIGH'),
    ('Priya Nair',     27, 'LOW'),
    ('Vikram Menon',   51, 'MEDIUM');

INSERT INTO sessions (patient_id) VALUES (1), (1), (2), (3);

\echo 'Schema created successfully.'