import { CalendarioComponent } from '../../shared/components/calendario/calendario';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { CarritoService } from '../../core/services/carrito.service';
import { CotizacionService } from '../../core/services/cotizacion.service';
import { AgendaService } from '../../core/services/agenda.service';
import { PagoService } from '../../core/services/pago.service';
import { Cotizacion } from '../../core/models/cotizacion.model';
import { AgendaBloque } from '../../core/models/agenda.model';

@Component({standalone:true,imports:[CalendarioComponent,CurrencyPipe,FormsModule,RouterLink],templateUrl:'./carrito.html',styleUrl:'./carrito.scss'})
export class CarritoComponent implements OnInit {
  carrito = inject(CarritoService);
  private cotizacionesApi = inject(CotizacionService);
  private agenda = inject(AgendaService);
  private pagos = inject(PagoService);
  cotizaciones = signal<Cotizacion[]>([]);
  items = computed(() => this.cotizaciones().filter(c => this.carrito.ids().includes(c.id)));
  total = computed(() => this.items().reduce((sum,c) => sum + Math.round(c.total),0));
  bloques = signal<AgendaBloque[]>([]);
  seleccion: Record<number,number> = {};
  cargando = signal(true); procesando = signal(false); error = signal('');
  agendaError = signal(false); cargandoAgenda = signal(false); confirmarVaciado = signal(false);
  renovando = signal<number | null>(null);
  listo = computed(() => !this.cargando() && !this.cargandoAgenda() && !this.procesando()
    && this.items().length > 0 && this.items().every(c => c.estado === 'PENDIENTE_PAGO') && this.total() > 0);
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    if (this.procesando()) return;
    this.cargando.set(true); this.error.set('');
    this.carrito.sincronizar().pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: c => { this.cotizaciones.set(c); if (this.items().some(x => x.requiereVisita)) this.cargarAgenda(); },
      error: () => this.error.set('No se pudo actualizar el carrito. Reintenta antes de pagar.'),
    });
  }
  cargarAgenda(): void {
    this.cargandoAgenda.set(true); this.agendaError.set(false);
    this.agenda.listarDisponibles().pipe(finalize(() => this.cargandoAgenda.set(false))).subscribe({
      next: b => {this.bloques.set(b);this.seleccion={};},error:() => {this.bloques.set([]);this.agendaError.set(true);},
    });
  }
  bloquesPara(id:number){return this.bloques().filter(b=>!this.ocupado(b.id,id));}
  ocupado(bloqueId:number, cotizacionId:number): boolean { return this.items().some(c => c.id !== cotizacionId && Number(this.seleccion[c.id]) === bloqueId); }
  quitar(id:number): void { if (this.procesando() || this.renovando()) return; this.carrito.quitar(id); delete this.seleccion[id]; this.error.set(''); }
  vaciar(): void { if (this.procesando() || this.renovando()) return;this.carrito.vaciar();this.seleccion={};this.confirmarVaciado.set(false);this.error.set(''); }
  renovar(c:Cotizacion): void {
    if (this.procesando() || this.renovando()) return;
    this.renovando.set(c.id);this.error.set('');
    this.cotizacionesApi.renovar(c.id).pipe(finalize(() => this.renovando.set(null))).subscribe({
      next:nueva => {this.carrito.agregar(nueva,c.id);this.cotizaciones.update(lista => [...lista.filter(x => x.id !== c.id),nueva]);delete this.seleccion[c.id];if(nueva.requiereVisita)this.cargarAgenda();},
      error:e => this.error.set(e.error?.mensaje ?? e.error?.error ?? 'No se pudo renovar la cotización. Revisa la disponibilidad en el catálogo.'),
    });
  }
  pagar(): void {
    if (this.procesando() || this.cargando() || this.renovando()) return;
    this.error.set('');
    if (!this.listo() || this.items().some(c => c.requiereVisita && !Number(this.seleccion[c.id]))) {
      this.error.set('Renueva las cotizaciones vencidas y selecciona un horario por cortina con servicios.');return;
    }
    const items=this.items().map(c => ({cotizacionId:c.id,bloqueId:c.requiereVisita ? Number(this.seleccion[c.id]) : null}));
    if (new Set(items.filter(i => i.bloqueId).map(i => i.bloqueId)).size !== items.filter(i => i.bloqueId).length) {
      this.error.set('Selecciona un horario distinto para cada cortina con servicios.');return;
    }
    this.procesando.set(true);
    this.pagos.iniciarCarrito(items).subscribe({
      next:r => { void this.pagos.redirigirAWebpay(r.url,r.token).catch(() => { this.procesando.set(false); this.error.set('No se pudo abrir Webpay. Revisa Mis pedidos antes de volver a pagar.'); }); },
      error:e => {this.procesando.set(false);this.error.set(e.error?.mensaje ?? e.error?.error ?? 'No se pudo iniciar el pago. Si ya pagaste, revisa Mis pedidos antes de reintentar.');if(e.status===409)this.cargarAgenda();},
    });
  }
}
