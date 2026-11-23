
package br.ufu.pds.dentista.tratamento;

import br.ufu.pds.dentista.paciente.Paciente;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plano_tratamento")
public class PlanoTratamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPlanoTratamento status =
            StatusPlanoTratamento.EM_ELABORACAO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(
        mappedBy = "plano",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("ordem ASC")
    private List<EtapaTratamento> etapas = new ArrayList<>();

    protected PlanoTratamento() {}

    public PlanoTratamento(String titulo, Paciente paciente) {
        this.titulo = titulo;
        this.paciente = paciente;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    public void adicionarEtapa(String procedimento) {
        verificarEmElaboracao();

        int ordem = etapas.size() + 1;
        etapas.add(new EtapaTratamento(this, procedimento, ordem));
    }

    public void verificarEmElaboracao() {
        if (status != StatusPlanoTratamento.EM_ELABORACAO) {
            throw new IllegalStateException(
                "O plano precisa estar em elaboração."
            );
        }
    }

    public void alterarStatus(StatusPlanoTratamento novoStatus) {
        this.status = novoStatus;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public Paciente getPaciente() { return paciente; }
    public StatusPlanoTratamento getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public List<EtapaTratamento> getEtapas() { return etapas; }
}
