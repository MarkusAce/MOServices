export interface Resena {
  id: number;
  usuarioId: number | null;
  nombre: string;
  descripcion: string;
  calidad: number;
  servicio: number;
  fecha: string;
  editada: boolean;
  fechaEdicion: string | null;
}
