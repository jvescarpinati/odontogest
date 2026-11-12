import { useEffect, useState } from 'react';
import { pacientesApi } from '../api.js';
import { formatarCpf, formatarData, formatarTelefone } from '../utils/formatters.js';

export default function PacienteList({ onNovo, onEditar }) {
  const [busca, setBusca] = useState('');
  const [pacientes, setPacientes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  // Busca com "debounce": espera 300 ms sem digitação antes de chamar a API,
  // para não disparar uma requisição a cada tecla.
  useEffect(() => {
    let cancelado = false;
    const timer = setTimeout(async () => {
      setCarregando(true);
      setErro('');
      try {
        const dados = await pacientesApi.listar(busca.trim());
        if (!cancelado) setPacientes(dados);
      } catch (e) {
        if (!cancelado) setErro(e.message);
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }, 300);
    return () => {
      cancelado = true;
      clearTimeout(timer);
    };
  }, [busca]);

  return (
    <section className="card">
      <div className="card-topo">
        <h1>Pacientes cadastrados</h1>
        <button className="btn primario" onClick={onNovo}>+ Novo paciente</button>
      </div>

      <input
        className="busca"
        type="search"
        placeholder="Buscar por nome ou CPF..."
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        autoFocus
      />

      {erro && <div className="alerta erro">{erro}</div>}

      {carregando ? (
        <p className="vazio">Carregando...</p>
      ) : pacientes.length === 0 && !erro ? (
        <p className="vazio">
          {busca ? 'Nenhum paciente encontrado para essa busca.' : 'Nenhum paciente cadastrado ainda.'}
        </p>
      ) : (
        <table className="tabela">
          <thead>
            <tr>
              <th>Nome</th>
              <th>CPF</th>
              <th>Nascimento</th>
              <th>Telefone</th>
              <th>E-mail</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {pacientes.map((p) => (
              <tr key={p.id}>
                <td>{p.nome}</td>
                <td className="mono">{formatarCpf(p.cpf)}</td>
                <td>{formatarData(p.dataNascimento)}</td>
                <td>{formatarTelefone(p.telefone)}</td>
                <td>{p.email || '—'}</td>
                <td className="acoes">
                  <button className="btn" onClick={() => onEditar(p.id)}>Editar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
