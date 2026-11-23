package br.ufu.pds.dentista.tratamento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EtapaTratamentoRepository
        extends JpaRepository<EtapaTratamento, Long> {

    List<EtapaTratamento> findByPlanoIdOrderByOrdemAsc(Long planoId);
}