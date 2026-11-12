import { useEffect, useState } from 'react';
import { pacientesApi } from '../api.js';
import { formatarCpf, formatarTelefone, hojeIso, somenteDigitos } from '../utils/formatters.js';
import { validarPaciente } from '../utils/validacao.js';

const VAZIO = { nome: '', cpf: '', dataNascimento: '', telefone: '', email: '', endereco: '' };

/** Serve para cadastro (sem id) e edição (com id). */
export default function PacienteForm({ id, onSalvo, onCancelar }) {
  const editando = id != null;
  const [form, setForm] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [carregando, setCarregando] = useState(editando);
  const [salvando, setSalvando] = useState(false);

  // Na edição, carrega os dados atuais do paciente
  useEffect(() => {
    if (!editando) return;
    pacientesApi
      .buscar(id)
      .then((p) =>
        setForm({
          nome: p.nome,
          cpf: formatarCpf(p.cpf),
          dataNascimento: p.dataNascimento,
          telefone: formatarTelefone(p.telefone),
          email: p.email ?? '',
          endereco: p.endereco ?? '',
        })
      )
      .catch((e) => setErroGeral(e.message))
      .finally(() => setCarregando(false));
  }, [id, editando]);

  function alterar(campo, valor) {
    if (campo === 'cpf') valor = formatarCpf(valor);
    if (campo === 'telefone') valor = formatarTelefone(valor);
    setForm((f) => ({ ...f, [campo]: valor }));
    setErros((e) => ({ ...e, [campo]: undefined })); // limpa o erro ao corrigir
  }

  async function salvar(e) {
    e.preventDefault();
    setErroGeral('');

    const errosLocais = validarPaciente(form);
    setErros(errosLocais);
    if (Object.keys(errosLocais).length > 0) return;

    const dados = {
      nome: form.nome.trim(),
      cpf: somenteDigitos(form.cpf),
      dataNascimento: form.dataNascimento,
      telefone: form.telefone,
      email: form.email.trim() || null,
      endereco: form.endereco.trim() || null,
    };

    setSalvando(true);
    try {
      if (editando) await pacientesApi.atualizar(id, dados);
      else await pacientesApi.criar(dados);
      onSalvo();
    } catch (err) {
      if (err.status === 400) {
        setErros(err.campos);         // erros por campo vindos do backend
        setErroGeral(err.message);
      } else if (err.status === 409) {
        setErros({ cpf: err.message }); // CPF duplicado
      } else {
        setErroGeral(err.message);
      }
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <section className="card"><p className="vazio">Carregando...</p></section>;

  return (
    <section className="card">
      <div className="card-topo">
        <h1>{editando ? 'Editar paciente' : 'Novo paciente'}</h1>
      </div>

      {erroGeral && <div className="alerta erro">{erroGeral}</div>}

      <form onSubmit={salvar} noValidate className="formulario">
        <Campo label="Nome completo" obrigatorio erro={erros.nome} largo>
          <input value={form.nome} maxLength={150} onChange={(e) => alterar('nome', e.target.value)} />
        </Campo>

        <Campo label="CPF" obrigatorio erro={erros.cpf}>
          <input value={form.cpf} inputMode="numeric" placeholder="000.000.000-00"
                 onChange={(e) => alterar('cpf', e.target.value)} />
        </Campo>

        <Campo label="Data de nascimento" obrigatorio erro={erros.dataNascimento}>
          <input type="date" value={form.dataNascimento} max={hojeIso()}
                 onChange={(e) => alterar('dataNascimento', e.target.value)} />
        </Campo>

        <Campo label="Telefone" obrigatorio erro={erros.telefone}>
          <input value={form.telefone} inputMode="tel" placeholder="(34) 99999-0000"
                 onChange={(e) => alterar('telefone', e.target.value)} />
        </Campo>

        <Campo label="E-mail" erro={erros.email}>
          <input type="email" value={form.email} maxLength={150}
                 onChange={(e) => alterar('email', e.target.value)} />
        </Campo>

        <Campo label="Endereço" erro={erros.endereco} largo>
          <input value={form.endereco} maxLength={255} placeholder="Rua, número, bairro, cidade - UF"
                 onChange={(e) => alterar('endereco', e.target.value)} />
        </Campo>

        <div className="botoes largo">
          <button type="button" className="btn" onClick={onCancelar} disabled={salvando}>Cancelar</button>
          <button type="submit" className="btn primario" disabled={salvando}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
        </div>
      </form>
    </section>
  );
}

function Campo({ label, obrigatorio, erro, largo, children }) {
  return (
    <label className={`campo ${largo ? 'largo' : ''} ${erro ? 'com-erro' : ''}`}>
      <span>{label}{obrigatorio && <em> *</em>}</span>
      {children}
      {erro && <small>{erro}</small>}
    </label>
  );
}
