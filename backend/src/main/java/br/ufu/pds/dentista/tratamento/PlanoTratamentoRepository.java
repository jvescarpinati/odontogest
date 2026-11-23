package br.ufu.pds.dentista.tratamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, Long> {

    @Override
    @EntityGraph(attributePaths = {"etapas", "paciente"})
    Optional<PlanoTratamento> findById(Long id);

    @EntityGraph(attributePaths = {"etapas", "paciente"})
    List<PlanoTratamento> findByPacienteId(Long pacienteId);
}