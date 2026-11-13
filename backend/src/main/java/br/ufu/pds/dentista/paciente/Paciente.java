package br.ufu.pds.dentista.paciente;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paciente",
       uniqueConstraints = @UniqueConstraint(name = "uk_paciente_cpf", columnNames = "cpf"))
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 11, columnDefinition = "char(11)")
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String endereco;

    /** Exclusão lógica: preserva o histórico de tratamentos ligados ao paciente. */
    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Paciente() { } // exigido pela JPA

    public Paciente(String nome, String cpf, LocalDate dataNascimento,
                    String telefone, String email, String endereco) {
        atualizarDados(nome, cpf, dataNascimento, telefone, email, endereco);
    }

    /** Único ponto de alteração dos dados cadastrais (encapsulamento). */
    public void atualizarDados(String nome, String cpf, LocalDate dataNascimento,
                               String telefone, String email, String endereco) {
        this.nome = nome.trim();
        this.cpf = CpfUtils.normalizar(cpf);
        this.dataNascimento = dataNascimento;
        this.telefone = telefone.trim();
        this.email = (email == null || email.isBlank()) ? null : email.trim().toLowerCase();
        this.endereco = (endereco == null || endereco.isBlank()) ? null : endereco.trim();
    }

    public void desativar() { this.ativo = false; }

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    void aoAtualizar() { atualizadoEm = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getEndereco() { return endereco; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
