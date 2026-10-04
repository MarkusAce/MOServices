import { etiquetaEstado } from '../../core/utils/estado-pedido';
import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { PedidoService } from '../../core/services/pedido.service';
import { Pedido } from '../../core/models/pedido.model';
import { CotizacionService } from '../../core/services/cotizacion.service';
import { Cotizacion } from '../../core/models/cotizacion.model';

@Component({
  selector: 'app-mis-pedidos',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './mis-pedidos.html',
  styleUrl: './mis-pedidos.scss',
})
export class MisPedidosComponent implements OnInit {
  private pedidoService = inject(PedidoService);
  private cotizacionService = inject(CotizacionService);
  private router = inject(Router);

  errorPedidos = signal('');pedidos = signal<Pedido[]>([]);
  cargando = signal(true);
  cotizaciones = signal<Cotizacion[]>([]);
  errorCotizaciones = signal(false);
  renovandoId = signal<number | null>(null);
  errorRenovacion = signal('');

  renovar(cotizacion: Cotizacion): void {
    this.renovandoId.set(cotizacion.id);
    this.errorRenovacion.set('');
    this.cotizacionService.renovar(cotizacion.id).subscribe({
      next: nueva => this.router.navigate(['/agenda'], { queryParams: { cotizacionId: nueva.id } }),
      error: error => {
        this.errorRenovacion.set(error.error?.mensaje ?? 'No se pudo recalcular. Revisa la disponibilidad del catálogo.');
        this.renovandoId.set(null);
      },
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);this.errorPedidos.set('');this.errorCotizaciones.set(false);
    this.cotizacionService.listarPropias().subscribe({
      next: cotizaciones => this.cotizaciones.set(cotizaciones.filter(c => c.estado === 'PENDIENTE_PAGO' || c.estado === 'EXPIRADA')),
      error: () => this.errorCotizaciones.set(true),
    });
    this.pedidoService.listarPropios().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: () => {this.errorPedidos.set('No se pudieron cargar los pedidos.');this.cargando.set(false);},
    });

    this.pedidoService.listarNotificaciones().subscribe({next: (notificaciones) => {
      if (notificaciones.length > 0) {
        this.pedidoService.marcarNotificacionesVistas().subscribe({error: () => this.errorRenovacion.set('No se pudieron actualizar las notificaciones.')});
      }
    }, error: () => this.errorRenovacion.set('No se pudieron consultar las notificaciones.')});
  }

  etiquetaEstado=etiquetaEstado;
}
