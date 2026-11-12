package br.ufu.pds.dentista.paciente;

import br.ufu.pds.dentista.common.ConflitoException;
import br.ufu.pds.dentista.common.NaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PacienteResponse criar(PacienteRequest req) {
        String cpf = CpfUtils.normalizar(req.cpf());
        if (repository.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um paciente cadastrado com este CPF");
        }
        Paciente p = new Paciente(req.nome(), cpf, req.dataNascimento(),
                req.telefone(), req.email(), req.endereco());
        return PacienteResponse.de(repository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(String busca) {
        List<Paciente> resultado;
        if (busca == null || busca.isBlank()) {
            resultado = repository.findByAtivoTrueOrderByNomeAsc();
        } else if (busca.matches("[\\d.\\-\\s]+")) {
            // só números/pontuação de CPF -> busca por CPF (prefixo)
            resultado = repository.findByAtivoTrueAndCpfStartingWithOrderByNomeAsc(CpfUtils.normalizar(busca));
        } else {
            resultado = repository.findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
        }
        return resultado.stream().map(PacienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorId(Long id) {
        return PacienteResponse.de(buscarAtivo(id));
    }

    @Transactional
    public PacienteResponse atualizar(Long id, PacienteRequest req) {
        Paciente p = buscarAtivo(id);
        String cpf = CpfUtils.normalizar(req.cpf());
        if (repository.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflitoException("Já existe outro paciente cadastrado com este CPF");
        }
        p.atualizarDados(req.nome(), cpf, req.dataNascimento(),
                req.telefone(), req.email(), req.endereco());
        return PacienteResponse.de(p); // dirty checking: a JPA salva ao fim da transação
    }

    @Transactional
    public void remover(Long id) {
        buscarAtivo(id).desativar();
    }

    private Paciente buscarAtivo(Long id) {
        return repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new NaoEncontradoException("Paciente não encontrado"));
    }
}
