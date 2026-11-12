-- US02: Anamnese e histórico clínico do paciente
CREATE TABLE IF NOT EXISTS anamnese (
    id                      BIGSERIAL PRIMARY KEY,
    paciente_id             BIGINT NOT NULL UNIQUE REFERENCES paciente(id),
    alergias                TEXT,
    condicoes_preexistentes TEXT,
    medicamentos_em_uso     TEXT,
    alertas_graves          TEXT,
    data_modificacao        TIMESTAMP NOT NULL DEFAULT NOW()
);
