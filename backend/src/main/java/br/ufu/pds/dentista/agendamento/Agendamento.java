package br.ufu.pds.dentista.agendamento;

import br.ufu.pds.dentista.paciente.Paciente;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade de agendamento de consultas (US04 — SCRUM-33).
 *
 * {@code etapaId} é nullable e sem FK real enquanto a US03 não existir;
 * quando o módulo de planos de tratamento for criado, basta adicionar
 * {@code @ManyToOne} aqui — o campo já estará no banco com o valor correto.
 */
@Entity
@Table(
    name = "agendamento",
    indexes = {
        @Index(name = "idx_agendamento_paciente_data", columnList = "paciente_id, data_hora_inicio"),
        @Index(name = "idx_agendamento_data_inicio",   columnList = "data_hora_inicio")
    }
)
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /** Reservado para US03 — sempre NULL até que o módulo de planos seja implementado. */
    @Column(name = "etapa_id")
    private Long etapaId;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.AGENDADA;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Agendamento() { }

    public Agendamento(Paciente paciente, LocalDateTime inicio, LocalDateTime fim, String observacoes) {
        this.paciente        = paciente;
        this.dataHoraInicio  = inicio;
        this.dataHoraFim     = fim;
        this.observacoes     = observacoes;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    // ── Negócio ──────────────────────────────────────────────────────────────

    public void atualizar(LocalDateTime inicio, LocalDateTime fim, String observacoes) {
        this.dataHoraInicio = inicio;
        this.dataHoraFim    = fim;
        this.observacoes    = observacoes;
    }

    public void cancelar() {
        this.status = StatusAgendamento.CANCELADA;
    }

    public void confirmar() {
        this.status = StatusAgendamento.CONFIRMADA;
    }

    public void realizar() {
        this.status = StatusAgendamento.REALIZADA;
    }

    public boolean isCancelado() {
        return this.status == StatusAgendamento.CANCELADA;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public void setId(Long id)               { this.id = id; }
    public Long getId()                      { return id; }
    public Paciente getPaciente()            { return paciente; }
    public Long getEtapaId()                 { return etapaId; }
    public void setEtapaId(Long etapaId) {
    this.etapaId = etapaId;
    }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public LocalDateTime getDataHoraFim()    { return dataHoraFim; }
    public StatusAgendamento getStatus()     { return status; }
    public String getObservacoes()           { return observacoes; }
    public LocalDateTime getCriadoEm()       { return criadoEm; }
    public LocalDateTime getAtualizadoEm()   { return atualizadoEm; }
}
