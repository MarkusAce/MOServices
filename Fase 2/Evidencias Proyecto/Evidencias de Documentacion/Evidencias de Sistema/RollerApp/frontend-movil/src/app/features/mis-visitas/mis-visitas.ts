import { etiquetaEstado } from '../../core/utils/estado-pedido';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { fechaChile } from '../../core/utils/fecha-chile';
import { ObservacionesVisitaComponent } from '../../shared/components/observaciones-visita/observaciones-visita';
import { Component, OnDestroy, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonBadge,
  IonSpinner, IonButton, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { navigateOutline } from 'ionicons/icons';
import { AgendaService } from '../../core/services/agenda.service';
import { PedidoService } from '../../core/services/pedido.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoPedido, Pedido } from '../../core/models/pedido.model';

@Component({
  selector: 'app-mis-visitas',
  imports: [FormsModule, ObservacionesVisitaComponent, DatePipe, IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonBadge, IonSpinner, IonButton, IonIcon],
  templateUrl: './mis-visitas.html',
  styleUrl: './mis-visitas.scss',
})
export class MisVisitasComponent implements OnDestroy {
  private pedidoService = inject(PedidoService);
  private agendaApi = inject(AgendaService);
  private toast = inject(ToastService);

  etiquetaEstado=etiquetaEstado;
  fecha = fechaChile();
  private consulta?: Subscription;
  cambiarFecha(fecha: string): void {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) return;
    this.fecha = fecha; this.cargar();
  }
  hoy(): void { this.cambiarFecha(fechaChile()); }
  ngOnDestroy(): void { this.consulta?.unsubscribe(); }
  visitas = signal<Pedido[]>([]);
  cargando = signal(true);
  errorCarga = signal(false);
  actualizandoId = signal<number | null>(null);

  private siguienteEstado: Partial<Record<EstadoPedido, EstadoPedido>> = {
    EN_CONFECCION: 'EN_TERRENO',
    REPROGRAMADO: 'EN_TERRENO',
    EN_TERRENO: 'REALIZADO',
  };

  constructor() {
    addIcons({ navigateOutline });
  }

  ionViewWillEnter(): void { this.fecha = fechaChile(); this.cargar(); }
  ionViewWillLeave(): void { this.consulta?.unsubscribe(); }

  iniciarAtencion(visita: Pedido): void {
    if (!visita.visitaId || visita.visitaEstado !== 'RESERVADO' || visita.atencionInicio || this.actualizandoId() !== null) return;
    this.actualizandoId.set(visita.id);
    this.agendaApi.iniciar(visita.visitaId).subscribe({
      next: () => { this.actualizandoId.set(null); this.cargar(); },
      error: e => { this.actualizandoId.set(null); this.toast.mostrar(e.error?.mensaje ?? e.error?.error ?? 'No se pudo iniciar la atención.', 'error'); },
    });
  }
  cargar(): void {
    this.consulta?.unsubscribe();
    this.cargando.set(true);
    this.visitas.set([]);
    this.errorCarga.set(false);
    this.consulta = this.pedidoService.listarMisVisitas(this.fecha).subscribe({
      next: (visitas) => {
        this.visitas.set(visitas);
        this.cargando.set(false);
      },
      error: () => { this.errorCarga.set(true); this.cargando.set(false); },
    });
  }

  siguienteEstadoDisponible(visita: Pedido): EstadoPedido | null {
    if (visita.estado === 'EN_TERRENO' && visita.visitaEstado !== 'COMPLETADO') return null;
    return this.siguienteEstado[visita.estado] ?? null;
  }

  registrarAvance(visita: Pedido): void {
    const nuevo = this.siguienteEstadoDisponible(visita);
    if (!nuevo || this.actualizandoId() !== null) return;
    this.actualizandoId.set(visita.id);

    this.pedidoService.cambiarEstado(visita.id, nuevo).subscribe({
      next: () => {
        this.toast.mostrar(`Visita #${visita.id} actualizada a ${nuevo}.`, 'exito');
        this.actualizandoId.set(null);
        this.cargar();
      },
      error: (error) => {
        this.actualizandoId.set(null);
        this.toast.mostrar(error.error?.mensaje ?? error.error?.error ?? 'No se pudo registrar el avance.', 'error');
      },
    });
  }

  completarVisita(visita: Pedido): void {
    if (!visita.visitaId || visita.visitaEstado !== 'RESERVADO' || this.actualizandoId() !== null) return;
    this.actualizandoId.set(visita.id);
    this.agendaApi.completar(visita.visitaId).subscribe({
      next: () => { this.actualizandoId.set(null); this.toast.mostrar('Visita completada.', 'exito'); this.cargar(); },
      error: e => { this.actualizandoId.set(null); this.toast.mostrar(e.error?.mensaje ?? e.error?.error ?? 'No se pudo completar la visita.', 'error'); },
    });
  }

  abrirRuta(direccion: string, comuna: string): void {
    const destino = encodeURIComponent(`${direccion}, ${comuna}, Chile`);
    window.open(`https://www.google.com/maps/search/?api=1&query=${destino}`, '_system');
  }
}
