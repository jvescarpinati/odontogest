# OdontoGest — Frontend

React + Vite. Telas de listagem/busca, cadastro e edição de pacientes (US01).

## Rodar

Pré-requisito: Node.js 18 ou mais novo (`node -v`).

```bash
cd frontend
npm install      # só na primeira vez
npm run dev
```

Abra http://localhost:5173. O backend precisa estar rodando em http://localhost:8080 —
o Vite repassa as chamadas `/api` para ele (ver `vite.config.js`).

## Estrutura

```
src/
├── main.jsx                 # ponto de entrada
├── App.jsx                  # alterna entre lista, cadastro e edição
├── api.js                   # chamadas HTTP ao backend
├── components/
│   ├── PacienteList.jsx     # listagem + busca por nome/CPF
│   └── PacienteForm.jsx     # formulário de cadastro e edição
└── utils/
    ├── formatters.js        # máscaras de CPF/telefone, datas
    └── validacao.js         # validação no navegador (mesmas regras do backend)
```
