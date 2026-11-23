-- US04 - SCRUM-33: Tabela de agendamentos (PostgreSQL)
-- etapa_id é nullable: pronto para ligação futura com plano_tratamento (US03).
-- Enquanto a US03 não existir, o campo permanece sempre NULL.

CREATE TABLE IF NOT EXISTS agendamento (
    id              BIGSERIAL       PRIMARY KEY,
    paciente_id     BIGINT          NOT NULL REFERENCES paciente(id),
    etapa_id        BIGINT          NULL,           -- FK futura para etapa_procedimento (US03)
    data_hora_inicio TIMESTAMP      NOT NULL,
    data_hora_fim    TIMESTAMP      NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'AGENDADA',
    observacoes     TEXT,
    criado_em       TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_agendamento_status
        CHECK (status IN ('AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA')),

    CONSTRAINT chk_agendamento_datas
        CHECK (data_hora_fim > data_hora_inicio)
);

-- Índice principal: buscar agendamentos ativos de um paciente ordenados por data
CREATE INDEX IF NOT EXISTS idx_agendamento_paciente_data
    ON agendamento (paciente_id, data_hora_inicio);

-- Índice para a verificação de conflito de horários (consultas por intervalo de tempo)
-- Filtra apenas agendamentos ativos (não cancelados) para verificar sobreposição.
CREATE INDEX IF NOT EXISTS idx_agendamento_intervalo_ativo
    ON agendamento (data_hora_inicio, data_hora_fim)
    WHERE status <> 'CANCELADA';

-- Índice de visão semanal/diária: busca por range de datas (agenda do dia/semana)
CREATE INDEX IF NOT EXISTS idx_agendamento_data_inicio
    ON agendamento (data_hora_inicio);
