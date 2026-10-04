export type EstadoComision = 'LIQUIDABLE' | 'PAGADA';

export interface Comision {
  id: number;
  pedidoId: number;
  usuarioId: number;
  usuarioNombre: string;
  rol: 'VENDEDOR' | 'TECNICO';
  porcentaje: number | null;
  monto: number;
  estado: EstadoComision;
  creadaEn: string;
  pagadaEn: string | null;
}
