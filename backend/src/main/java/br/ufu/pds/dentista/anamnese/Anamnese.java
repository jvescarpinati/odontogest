package br.ufu.pds.dentista.anamnese;

import br.ufu.pds.dentista.paciente.Paciente;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anamnese")
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    private String alergias;
    private String condicoesPreexistentes;
    private String medicamentosEmUso;
    private String alertasGraves;
    private LocalDateTime dataModificacao;

    public Anamnese() {}

    public Anamnese(Paciente paciente, String alergias, String condicoes, String medicamentos, String alertas) {
        this.paciente = paciente;
        this.alergias = alergias;
        this.condicoesPreexistentes = condicoes;
        this.medicamentosEmUso = medicamentos;
        this.alertasGraves = alertas;
        this.dataModificacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public String getCondicoesPreexistentes() { return condicoesPreexistentes; }
    public void setCondicoesPreexistentes(String condicoesPreexistentes) { this.condicoesPreexistentes = condicoesPreexistentes; }
    public String getMedicamentosEmUso() { return medicamentosEmUso; }
    public void setMedicamentosEmUso(String medicamentosEmUso) { this.medicamentosEmUso = medicamentosEmUso; }
    public String getAlertasGraves() { return alertasGraves; }
    public void setAlertasGraves(String alertasGraves) { this.alertasGraves = alertasGraves; }
    public LocalDateTime getDataModificacao() { return dataModificacao; }
    public void setDataModificacao(LocalDateTime dataModificacao) { this.dataModificacao = dataModificacao; }
}
