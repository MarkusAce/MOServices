export type EstadoPedido = 'PAGADO' | 'EN_CONFECCION' | 'EN_TERRENO' | 'REALIZADO' | 'REPROGRAMADO' | 'CANCELADO';

export interface Pedido {
  atencionInicio: string | null;
  atencionFin: string | null;
  clienteId: number;
  direccionPendienteVerificacion: boolean;
  referenciasDireccion: string | null;
  id: number;
  cotizacionId: number;
  productoNombre: string;
  total: number;
  direccion: string;
  comuna: string;
  estado: EstadoPedido;
  visto: boolean;
  creadoEn: string;
  vendedorId: number | null;
  vendedorNombre: string | null;
  tecnicoId: number | null;
  tecnicoNombre: string | null;
  visitaId: number | null;
  visitaFecha: string | null;
  visitaHoraInicio: string | null;
  visitaHoraFin: string | null;
  visitaEstado: string | null;
  requiereVisita: boolean;
}
