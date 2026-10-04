
export function fechaChile(instante = new Date()): string {
 const partes = new Intl.DateTimeFormat('en-US', {timeZone: 'America/Santiago', year: 'numeric', month: '2-digit', day: '2-digit'}).formatToParts(instante);
 const valor = (tipo: string) => partes.find(p => p.type === tipo)!.value;
 return `${valor('year')}-${valor('month')}-${valor('day')}`;
}
