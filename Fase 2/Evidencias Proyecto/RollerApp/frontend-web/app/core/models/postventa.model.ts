export type EstadoPostventa = 'PENDIENTE' | 'RESUELTA';

export interface PostventaSolicitud {
  id: number;
  usuarioId: number | null;
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  comuna: string;
  descripcion: string;
  estado: EstadoPostventa;
  fecha: string;
}
