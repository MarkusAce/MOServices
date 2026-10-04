import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  mensaje: string;
  tipo: 'exito' | 'error';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private contador = 0;
  toasts = signal<Toast[]>([]);

  mostrar(mensaje: string, tipo: 'exito' | 'error' = 'exito'): void {
    const id = ++this.contador;
    this.toasts.update((lista) => [...lista, { id, mensaje, tipo }]);
    setTimeout(() => this.cerrar(id), 4000);
  }

  cerrar(id: number): void {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }
}
