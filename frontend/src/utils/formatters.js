export const somenteDigitos = (valor = '') => valor.replace(/\D/g, '');

/** "52998224725" -> "529.982.247-25" (aplica a máscara conforme o usuário digita) */
export function formatarCpf(valor = '') {
  const d = somenteDigitos(valor).slice(0, 11);
  return d
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
}

/** "34999990000" -> "(34) 99999-0000"; fixo com 10 dígitos -> "(34) 3333-0000" */
export function formatarTelefone(valor = '') {
  const d = somenteDigitos(valor).slice(0, 11);
  if (d.length <= 2) return d.length ? `(${d}` : '';
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}

/** "1990-05-20" -> "20/05/1990" */
export function formatarData(iso) {
  if (!iso) return '';
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

/** Data de hoje em yyyy-MM-dd, no fuso local (para o atributo max do input date). */
export function hojeIso() {
  const h = new Date();
  const mm = String(h.getMonth() + 1).padStart(2, '0');
  const dd = String(h.getDate()).padStart(2, '0');
  return `${h.getFullYear()}-${mm}-${dd}`;
}
