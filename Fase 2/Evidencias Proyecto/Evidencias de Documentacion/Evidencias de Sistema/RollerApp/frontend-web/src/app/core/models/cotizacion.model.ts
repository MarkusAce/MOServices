export type EstadoCotizacion = 'PENDIENTE_PAGO' | 'PAGADA' | 'EXPIRADA' | 'CANCELADA';

export interface Cotizacion {
  id: number;
  productoId: number;
  productoNombre: string;
  telaId: number;
  telaNombre: string;
  mecanismoId: number;
  mecanismoNombre: string;
  anchoCm: number;
  altoCm: number;
  metrosCuadrados: number;
  metrosCuadradosFacturados: number;
  aplicaCobroMinimo: boolean;
  mensajeCobroMinimo: string | null;
  valorTela: number;
  valorMecanismo: number;
  valorServicios: number;
  total: number;
  direccion: string;
  comuna: string;
  estado: EstadoCotizacion;
  creadoEn: string;
  expiraEn: string;
  requiereVisita: boolean;
  servicios?: { id: number; nombre: string; precio: number }[];
}

export interface CrearCotizacionRequest {
  productoId: number;
  telaId: number;
  mecanismoId: number;
  anchoCm: number;
  altoCm: number;
  direccion: string;
  comuna: string;
  servicioIds: number[];
  placeId?: string | null;
  direccionManual?: boolean;
  referenciasDireccion?: string;
}
