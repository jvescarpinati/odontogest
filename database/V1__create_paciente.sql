-- US01 - Tabela de pacientes (PostgreSQL)
-- Se o projeto usa spring.jpa.hibernate.ddl-auto=update, o Hibernate cria a tabela
-- a partir da entidade Paciente; este script serve como documentação e para Flyway.

CREATE TABLE IF NOT EXISTS paciente (
    id               BIGSERIAL     PRIMARY KEY,
    nome             VARCHAR(150)  NOT NULL,
    cpf              CHAR(11)      NOT NULL,          -- só dígitos, sem máscara
    data_nascimento  DATE          NOT NULL,
    telefone         VARCHAR(20)   NOT NULL,
    email            VARCHAR(150),
    endereco         VARCHAR(255),
    ativo            BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em        TIMESTAMP     NOT NULL DEFAULT NOW(),
    atualizado_em    TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_paciente_cpf UNIQUE (cpf)
);

-- Acelera a busca por nome (critério: busca rápida por nome ou CPF).
-- A busca por CPF já usa o índice criado pela constraint UNIQUE.
CREATE INDEX IF NOT EXISTS idx_paciente_nome ON paciente (LOWER(nome));
