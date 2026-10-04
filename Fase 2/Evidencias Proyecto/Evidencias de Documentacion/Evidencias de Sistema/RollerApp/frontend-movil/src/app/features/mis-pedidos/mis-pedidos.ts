import { CarritoService } from '../../core/services/carrito.service';
import { etiquetaEstado } from '../../core/utils/estado-pedido';
import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { CotizacionService } from '../../core/services/cotizacion.service';
import { Cotizacion } from '../../core/models/cotizacion.model';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonBadge, IonSpinner,
} from '@ionic/angular';
import { PedidoService } from '../../core/services/pedido.service';
import { Pedido } from '../../core/models/pedido.model';

@Component({
  selector: 'app-mis-pedidos',
  imports: [CurrencyPipe, DatePipe, IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonBadge, IonSpinner],
  templateUrl: './mis-pedidos.html',
})
export class MisPedidosComponent  {
  private pedidoService = inject(PedidoService);
  private cotizacionService = inject(CotizacionService);
  private router = inject(Router);
  private carrito = inject(CarritoService);

  etiquetaEstado=etiquetaEstado;
  pedidos = signal<Pedido[]>([]);
  cargando = signal(true);
  cotizaciones = signal<Cotizacion[]>([]);
  renovandoId = signal<number | null>(null);
  errorRenovacion = signal('');
  errorPedidos = signal('');

  irAgenda(id: number): void {
    this.router.navigate(['/agenda'], { queryParams: { cotizacionId: id } });
  }

  renovar(c: Cotizacion): void {
    if (this.renovandoId() !== null) return;
    this.renovandoId.set(c.id);
    this.errorRenovacion.set('');
    this.cotizacionService.renovar(c.id).subscribe({
      next: nueva => this.router.navigate(['/agenda'], { queryParams: { cotizacionId: nueva.id } }),
      error: error => {
        this.errorRenovacion.set(error.error?.mensaje ?? 'No se pudo recalcular la cotización.');
        this.renovandoId.set(null);
      },
    });
  }

  ionViewWillEnter(): void {
    this.cargando.set(true);
    this.errorPedidos.set('');
    this.errorRenovacion.set('');
    this.carrito.sincronizar().subscribe({error: () => {}});
    this.cotizacionService.listarPropias().subscribe({
      next: items => this.cotizaciones.set(items.filter(c => c.estado === 'PENDIENTE_PAGO' || c.estado === 'EXPIRADA')),
      error: () => this.errorRenovacion.set('No se pudieron cargar las cotizaciones.'),
    });
    this.pedidoService.listarPropios().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: () => { this.errorPedidos.set('No se pudieron cargar los pedidos. Vuelve a intentarlo.'); this.cargando.set(false); },
    });

    this.pedidoService.listarNotificaciones().subscribe({next: (notificaciones) => {
      if (notificaciones.length > 0) this.pedidoService.marcarNotificacionesVistas().subscribe({error: () => this.errorRenovacion.set('No se pudieron actualizar las notificaciones.')});
    }, error: () => this.errorRenovacion.set('No se pudieron consultar las notificaciones.')});
  }
}
