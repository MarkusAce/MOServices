import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { tap } from 'rxjs';
import { AuthService } from './auth.service';
import { CotizacionService } from './cotizacion.service';
import { Cotizacion } from '../models/cotizacion.model';

@Injectable({providedIn:'root'})
export class CarritoService {
  private auth = inject(AuthService);
  private api = inject(CotizacionService);
  ids = signal<number[]>([]);
  cantidad = computed(() => this.ids().length);
  constructor() {
    effect(onCleanup => {
      const usuarioId = this.auth.usuarioActual()?.id;
      this.ids.set(this.leer(usuarioId));
      if (usuarioId) {
        const suscripcion = this.sincronizar().subscribe({error:() => {}});
        onCleanup(() => suscripcion.unsubscribe());
      }
    });
  }
  private clave(id?: number): string { return `rollerapp_carrito_${id ?? 'anonimo'}`; }
  private leer(id?: number): number[] {
    if (!id) return [];
    try {
      const datos: unknown = JSON.parse(localStorage.getItem(this.clave(id)) ?? '[]');
      return Array.isArray(datos) ? [...new Set(datos.filter(x => Number.isSafeInteger(x) && x > 0))].slice(0,20) : [];
    } catch { return []; }
  }
  private guardar(ids: number[]): void {
    const id = this.auth.usuarioActual()?.id;
    if (!id) return;
    this.ids.set(ids);
    try { localStorage.setItem(this.clave(id),JSON.stringify(ids)); } catch {}
  }
  contiene(id: number): boolean { return this.ids().includes(id); }
  agregar(c: Cotizacion, reemplazarId?: number): void {
    const ids = this.ids();
    const nuevos = ids.filter(id => id !== reemplazarId);
    if (!nuevos.includes(c.id)) nuevos.push(c.id);
    if (nuevos.length > 20) throw new Error('El carrito admite hasta 20 cortinas.');
    this.guardar(nuevos);
  }
  quitar(id: number): void { this.guardar(this.ids().filter(x => x !== id)); }
  vaciar(): void { this.guardar([]); }
  sincronizar() {
    const usuarioId = this.auth.usuarioActual()?.id;
    return this.api.listarPropias().pipe(tap(cotizaciones => {
      if (this.auth.usuarioActual()?.id !== usuarioId) return;
      const guardados = this.ids();
      this.guardar(guardados.filter(id => cotizaciones.some(c => c.id === id && (c.estado === 'PENDIENTE_PAGO' || c.estado === 'EXPIRADA'))));
    }));
  }
}
