import { CotizacionesAdminComponent } from '../../../shared/components/cotizaciones-admin/cotizaciones-admin';
import { VisitaPedidoComponent } from '../../../shared/components/visita-pedido/visita-pedido';
import { RouterLink } from '@angular/router';
import { etiquetaEstado } from '../../../core/utils/estado-pedido';
import { ObservacionesVisitaComponent } from '../../../shared/components/observaciones-visita/observaciones-visita';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { Usuario } from '../../../core/models/usuario.model';
import { PedidoService } from '../../../core/services/pedido.service';
import { ToastService } from '../../../core/services/toast.service';
import { EstadoPedido, Pedido } from '../../../core/models/pedido.model';

@Component({
  selector: 'app-admin-pedidos',
  imports: [CotizacionesAdminComponent, VisitaPedidoComponent, RouterLink, ObservacionesVisitaComponent, DatePipe],
  templateUrl: './admin-pedidos.html',
  styleUrl: './admin-pedidos.scss',
})
export class AdminPedidosComponent implements OnInit {
  private pedidoService = inject(PedidoService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);
  vendedores = signal<Usuario[]>([]);
  esAdmin = this.auth.esAdmin;
  esVendedor = this.auth.esVendedor;

  pedidos = signal<Pedido[]>([]);
  cargando = signal(true);
  errorCarga = signal(false);
  cancelando = signal<Pedido|null>(null);
  actualizandoId = signal<number | null>(null);

  private siguienteEstado: Record<string, EstadoPedido> = {
    PAGADO: 'EN_CONFECCION',
    EN_CONFECCION: 'EN_TERRENO',
    EN_TERRENO: 'REALIZADO',
    REPROGRAMADO: 'EN_TERRENO',
  };

  ngOnInit(): void {
    this.cargar();
    if (this.esAdmin()) this.auth.listarUsuarios().subscribe({
      next: usuarios => this.vendedores.set(usuarios.filter(u => u.rol === 'VENDEDOR' && u.activo)),
      error: () => this.toast.mostrar('No se pudieron cargar vendedores.', 'error')
    });
  }

  etiquetaEstado=etiquetaEstado;
  cargar(): void {
    this.errorCarga.set(false);
    this.pedidoService.listarTodos().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: () => { this.errorCarga.set(true); this.cargando.set(false); },
    });
  }

  siguienteEstadoDisponible(pedido: Pedido): EstadoPedido | null {
    if (pedido.estado === 'EN_TERRENO' && pedido.requiereVisita && pedido.visitaEstado !== 'COMPLETADO') return null;
    if (pedido.estado === 'EN_CONFECCION' && !pedido.requiereVisita) return 'REALIZADO';
    return this.siguienteEstado[pedido.estado] ?? null;
  }

  asignarVendedor(pedido: Pedido, value: string): void {
    const vendedorId = Number(value);
    if (!vendedorId) return;
    this.pedidoService.asignarVendedor(pedido.id, vendedorId).subscribe({
      next: () => { this.toast.mostrar('Vendedor asignado.', 'exito'); this.cargar(); },
      error: e => this.toast.mostrar(e.error?.error ?? 'No se pudo asignar vendedor.', 'error')
    });
  }

  cancelarPedido():void {
    const p=this.cancelando();if(!p || this.actualizandoId()!==null)return;
    this.actualizandoId.set(p.id);this.pedidoService.cambiarEstado(p.id,'CANCELADO').subscribe({next:()=>{this.actualizandoId.set(null);this.cancelando.set(null);this.cargar();},error:e=>{this.actualizandoId.set(null);this.toast.mostrar(e.error?.mensaje ?? e.error?.error ?? 'No se pudo cancelar el pedido.','error');}});
  }
  avanzarEstado(pedido: Pedido): void {
    const nuevo = this.siguienteEstadoDisponible(pedido);
    if (!nuevo || this.actualizandoId() !== null) return;
    this.actualizandoId.set(pedido.id);

    this.pedidoService.cambiarEstado(pedido.id, nuevo).subscribe({
      next: () => {
        this.toast.mostrar(`Pedido #${pedido.id} actualizado.`, 'exito');
        this.actualizandoId.set(null);
        this.cargar();
      },
      error: (error) => {
        this.actualizandoId.set(null);
        this.toast.mostrar(error.error?.mensaje ?? error.error?.error ?? 'No se pudo cambiar el estado.', 'error');
      },
    });
  }
}
