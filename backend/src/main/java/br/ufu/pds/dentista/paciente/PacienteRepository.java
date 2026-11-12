package br.ufu.pds.dentista.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByIdAndAtivoTrue(Long id);

    List<Paciente> findByAtivoTrueOrderByNomeAsc();

    List<Paciente> findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    List<Paciente> findByAtivoTrueAndCpfStartingWithOrderByNomeAsc(String cpf);

    boolean existsByCpf(String cpf);

    /** No PUT: existe OUTRO paciente com este CPF? */
    boolean existsByCpfAndIdNot(String cpf, Long id);
}
