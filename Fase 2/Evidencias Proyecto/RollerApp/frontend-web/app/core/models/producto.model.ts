export type Categoria = 'BLACKOUT' | 'CLASICA' | 'MODERNA' | 'PANEL';

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  categoria: Categoria;
  imagenPrincipal: string | null;
  imagenes: string[];
  telaDefectoId: number | null;
  mecanismoDefectoId: number | null;
  anchoMaxCm: number;
  altoMaxCm: number;
  activo: boolean;
}

export type PasoLuz = 'OPACO' | 'FILTRANTE' | 'TRANSLUCIDO' | 'REGULABLE';

export interface Tela {
  id: number;
  nombre: string;
  descripcion: string | null;
  precioM2: number;
  pasoLuz: PasoLuz;
  activo: boolean;
}

export interface Mecanismo {
  id: number;
  nombre: string;
  descripcion: string | null;
  valorFijo: number;
  activo: boolean;
}

export interface ServicioAdicional {
  id: number;
  comisionTecnico: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  activo: boolean;
}
