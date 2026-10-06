export type EstadoAgenda = 'DISPONIBLE' | 'PRE_RESERVADO' | 'RESERVADO' | 'COMPLETADO' | 'CANCELADO';

export interface AgendaBloque {
  id: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoAgenda;
}
