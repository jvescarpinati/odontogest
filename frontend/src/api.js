// Comunicação com o backend. O Vite repassa /api para http://localhost:8080 (ver vite.config.js).
const BASE = '/api/pacientes';

export class ApiError extends Error {
  constructor(mensagem, status, campos = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos; // erros por campo vindos do GlobalExceptionHandler
  }
}

async function request(url, options = {}) {
  let res;
  try {
    res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor. O backend está rodando?', 0);
  }

  if (res.status === 204) return null;

  const body = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(body?.mensagem || `Erro ${res.status}`, res.status, body?.campos || {});
  }
  return body;
}

export const pacientesApi = {
  listar: (busca) =>
    request(busca ? `${BASE}?busca=${encodeURIComponent(busca)}` : BASE),
  buscar: (id) => request(`${BASE}/${id}`),
  criar: (dados) => request(BASE, { method: 'POST', body: JSON.stringify(dados) }),
  atualizar: (id, dados) =>
    request(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(dados) }),
};
