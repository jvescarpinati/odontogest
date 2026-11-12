import { useState } from 'react';
import PacienteList from './components/PacienteList.jsx';
import PacienteForm from './components/PacienteForm.jsx';

/**
 * Navegação simples por estado, sem biblioteca de rotas:
 *  { tela: 'lista' } | { tela: 'novo' } | { tela: 'editar', id }
 */
export default function App() {
  const [view, setView] = useState({ tela: 'lista' });
  const [aviso, setAviso] = useState('');

  function navegar(novaView) {
    setAviso(''); // o aviso de sucesso só faz sentido na lista, logo após salvar
    setView(novaView);
  }

  function aoSalvar(mensagem) {
    setView({ tela: 'lista' });
    setAviso(mensagem);
  }

  return (
    <>
      <header className="topo">
        <span className="logo">🦷 OdontoGest</span>
        <span className="subtitulo">Pacientes</span>
      </header>

      <main className="conteudo">
        {aviso && <div className="alerta sucesso">{aviso}</div>}

        {view.tela === 'lista' && (
          <PacienteList
            onNovo={() => navegar({ tela: 'novo' })}
            onEditar={(id) => navegar({ tela: 'editar', id })}
          />
        )}
        {view.tela === 'novo' && (
          <PacienteForm
            onSalvo={() => aoSalvar('Paciente cadastrado com sucesso.')}
            onCancelar={() => navegar({ tela: 'lista' })}
          />
        )}
        {view.tela === 'editar' && (
          <PacienteForm
            id={view.id}
            onSalvo={() => aoSalvar('Dados do paciente atualizados.')}
            onCancelar={() => navegar({ tela: 'lista' })}
          />
        )}
      </main>
    </>
  );
}
