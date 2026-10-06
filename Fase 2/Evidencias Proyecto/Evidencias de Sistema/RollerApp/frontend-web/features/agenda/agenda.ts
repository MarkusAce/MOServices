import { Subscription } from 'rxjs';
import { CalendarioComponent } from '../../shared/components/calendario/calendario';
import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AgendaService } from '../../core/services/agenda.service';
import { CotizacionService } from '../../core/services/cotizacion.service';
import { PagoService } from '../../core/services/pago.service';
import { ToastService } from '../../core/services/toast.service';
import { AgendaBloque } from '../../core/models/agenda.model';

@Component({
  selector: 'app-agenda',
  imports: [CalendarioComponent],
  templateUrl: './agenda.html',
  styleUrl: './agenda.scss',
})
export class AgendaComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private agendaService = inject(AgendaService);
  private pagoService = inject(PagoService);
  private cotizacionService = inject(CotizacionService);
  private toast = inject(ToastService);

  bloques = signal<AgendaBloque[]>([]);
  cargando = signal(true);
  procesando = signal(false);
  cotizacionId = 0;
  errorCobertura = signal('');
  requiereVisita = signal(true);
  errorCarga = signal(false);

  private parametros?: Subscription;
  private cargas = new Subscription();
  ngOnDestroy(): void { this.parametros?.unsubscribe(); this.cargas.unsubscribe(); }
  ngOnInit(): void {
    this.parametros = this.route.queryParamMap.subscribe(params => this.cargarCotizacion(Number(params.get('cotizacionId'))));
  }
  private cargarCotizacion(id: number): void {
    this.cargas.unsubscribe(); this.cargas = new Subscription();
    this.cotizacionId = id; this.bloques.set([]); this.cargando.set(true); this.procesando.set(false);
    this.errorCarga.set(false); this.errorCobertura.set(''); this.requiereVisita.set(true);
    if (!Number.isSafeInteger(this.cotizacionId) || this.cotizacionId <= 0) {
      this.toast.mostrar('Falta indicar la cotización a agendar.', 'error');
      this.router.navigateByUrl('/catalogo');
      return;
    }

    this.cargas.add(this.cotizacionService.obtenerPorId(this.cotizacionId).subscribe({
      next: cotizacion => {
        this.requiereVisita.set(cotizacion.requiereVisita);
        if (!cotizacion.requiereVisita) {
          this.cargando.set(false);
          return;
        }
        this.recargarDisponibles();
      },
      error: error => {
        this.errorCarga.set(true);
        this.toast.mostrar(error.error?.mensaje ?? 'No se pudo cargar la cotización.', 'error');
        this.cargando.set(false);
      },
    }));
  }

  elegirBloque(bloqueId: number): void {
    if (this.procesando() || this.cargando()) return;
    this.errorCobertura.set('');
    this.procesando.set(true);

    this.agendaService.preReservar(bloqueId, this.cotizacionId).subscribe({
      next: () => this.irAPagar(),
      error: (error) => {
        const mensaje = error.error?.mensaje ?? error.error?.error ?? 'No se pudo reservar ese horario.';
        if (error.status === 400 && mensaje.includes('visitas técnicas')) this.errorCobertura.set(mensaje);
        this.toast.mostrar(mensaje, 'error');
        this.procesando.set(false);

        if (error.status === 409) this.recargarDisponibles();
      },
    });
  }

  private recargarDisponibles(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    this.cargas.add(this.agendaService.listarDisponibles().subscribe({
      next: bloques => { this.bloques.set(bloques); this.cargando.set(false); },
      error: () => { this.errorCarga.set(true); this.bloques.set([]); this.cargando.set(false); },
    }));
  }

  pagarSinVisita(): void {
    if (this.requiereVisita() || this.cargando() || this.errorCarga() || this.procesando()) return;
    this.procesando.set(true);
    this.irAPagar();
  }

  private irAPagar(): void {
    this.pagoService.iniciar(this.cotizacionId).subscribe({
      next: respuesta => { void this.pagoService.redirigirAWebpay(respuesta.url, respuesta.token).catch(() => { this.procesando.set(false); this.toast.mostrar('No se pudo abrir Webpay. Revisa Mis pedidos antes de volver a pagar.', 'error'); }); },
      error: (error) => {
        this.toast.mostrar(error.error?.mensaje ?? error.error?.error ?? 'No se pudo iniciar el pago.', 'error');
        this.procesando.set(false);
      },
    });
  }
}
