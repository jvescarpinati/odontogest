import { somenteDigitos, hojeIso } from './formatters.js';

/** Mesmo algoritmo do CpfUtils.java (módulo 11). O backend continua sendo quem decide. */
export function cpfValido(cpf) {
  const d = somenteDigitos(cpf);
  if (d.length !== 11 || /^(\d)\1{10}$/.test(d)) return false;
  const digito = (qtd) => {
    let soma = 0;
    for (let i = 0; i < qtd; i++) soma += Number(d[i]) * (qtd + 1 - i);
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };
  return digito(9) === Number(d[9]) && digito(10) === Number(d[10]);
}

/**
 * Validação no navegador: dá retorno imediato ao usuário sem ir ao servidor.
 * Retorna { campo: mensagem } — vazio se estiver tudo certo.
 */
export function validarPaciente(p) {
  const erros = {};
  if (!p.nome.trim()) erros.nome = 'Nome é obrigatório';
  if (!somenteDigitos(p.cpf)) erros.cpf = 'CPF é obrigatório';
  else if (!cpfValido(p.cpf)) erros.cpf = 'CPF inválido';
  if (!p.dataNascimento) erros.dataNascimento = 'Data de nascimento é obrigatória';
  else if (p.dataNascimento >= hojeIso()) erros.dataNascimento = 'Data de nascimento deve estar no passado';
  const tel = somenteDigitos(p.telefone);
  if (!tel) erros.telefone = 'Telefone é obrigatório';
  else if (tel.length < 10) erros.telefone = 'Telefone inválido';
  if (p.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(p.email.trim())) erros.email = 'E-mail inválido';
  return erros;
}
