package br.ufu.pds.dentista.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Todos os agendamentos de um paciente, em ordem cronológica. */
    List<Agendamento> findByPacienteIdOrderByDataHoraInicioAsc(Long pacienteId);

    /**
     * Agenda do dia / da semana: agendamentos cujo início cai dentro do intervalo.
     * O FETCH evita N+1 ao renderizar o nome do paciente na tela.
     */
    @Query("""
           SELECT a FROM Agendamento a
           JOIN FETCH a.paciente
           WHERE a.dataHoraInicio >= :inicio
             AND a.dataHoraInicio <  :fim
           ORDER BY a.dataHoraInicio ASC
           """)
    List<Agendamento> findByIntervalo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

    /**
     * Conta agendamentos ATIVOS que se sobrepõem ao intervalo [novoInicio, novoFim).
     *
     * Dois intervalos [A,B) e [C,D) se sobrepõem quando: A < D && C < B.
     * Bordas compartilhadas (fim de um == início do outro) NÃO contam como conflito.
     *
     * @param novoInicio     início do agendamento a verificar
     * @param novoFim        fim do agendamento a verificar
     * @param excludeId      id do próprio agendamento (para o PUT não conflitar consigo mesmo); usar 0L no POST
     * @param statusCancelado valor do enum CANCELADA — passado como parâmetro para compatibilidade com H2
     */
    @Query("""
           SELECT COUNT(a) FROM Agendamento a
           WHERE a.status      <> :statusCancelado
             AND a.id          <> :excludeId
             AND a.dataHoraInicio < :novoFim
             AND a.dataHoraFim    > :novoInicio
           """)
    long contarConflitos(
            @Param("novoInicio")      LocalDateTime novoInicio,
            @Param("novoFim")         LocalDateTime novoFim,
            @Param("excludeId")       Long excludeId,
            @Param("statusCancelado") StatusAgendamento statusCancelado);

    /** Verifica se existe algum agendamento vinculado a uma etapa de tratamento. */
    boolean existsByEtapaId(Long etapaId);

}
