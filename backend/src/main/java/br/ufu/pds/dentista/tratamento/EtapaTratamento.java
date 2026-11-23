package br.ufu.pds.dentista.tratamento;

import jakarta.persistence.*;

@Entity
@Table(name = "etapa_tratamento")
public class EtapaTratamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoTratamento plano;

    @Column(nullable = false, length = 200)
    private String procedimento;

    @Column(nullable = false)
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEtapa status = StatusEtapa.PENDENTE;

    protected EtapaTratamento() {}

    public EtapaTratamento(
            PlanoTratamento plano,
            String procedimento,
            Integer ordem) {

        this.plano = plano;
        this.procedimento = procedimento;
        this.ordem = ordem;
    }

    public void alterarOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Long getId() {
        return id;
    }

    public PlanoTratamento getPlano() {
        return plano;
    }

    public String getProcedimento() {
        return procedimento;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public StatusEtapa getStatus() {
        return status;
    }
}