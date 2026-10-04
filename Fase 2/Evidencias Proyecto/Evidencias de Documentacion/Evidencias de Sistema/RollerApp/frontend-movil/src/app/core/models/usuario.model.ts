export type Rol = 'CLIENTE' | 'VENDEDOR' | 'TECNICO' | 'ADMIN';

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  rol: Rol;
  activo: boolean;
}

export interface LoginResponse {
  token: string;
  usuario: Usuario;
}

export interface MensajeRespuesta {
  mensaje: string;
}
