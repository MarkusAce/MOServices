import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AgendaBloque, } from '../../../core/models/agenda.model';
import { AgendaService } from '../../../core/services/agenda.service';
import { IncidenciaPago, IncidenciasPagoService } from '../../../core/services/incidencias-pago.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-incidencias',
  imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, FormsModule],
  templateUrl: './admin-incidencias.html',
  styleUrl: './admin-incidencias.scss',
})
export class AdminIncidenciasComponent  {
  private api = inject(IncidenciasPagoService);
  private agenda = inject(AgendaService);
  private toast = inject(ToastService);
  incidencias = signal<IncidenciaPago[]>([]);
  disponibles = signal<AgendaBloque[]>([]);
  errorCarga = signal('');
  cargando = signal(false);
  procesando = signal(false);
  seleccion: Record<number, number | null> = {};

  ionViewWillEnter(): void { this.cargar(); }

  cargar(): void {
    this.errorCarga.set('');
    this.cargando.set(true);
    this.api.listar().subscribe({
      next: datos => { this.incidencias.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.errorCarga.set('No se pudieron cargar las incidencias.'); },
    });
    this.agenda.listarDisponibles().subscribe({
      next: bloques => this.disponibles.set(bloques),
      error: () => this.toast.mostrar('No se pudieron cargar los horarios.', 'error'),
    });
  }

  reagendar(pedidoId: number): void {
    const bloqueId = this.seleccion[pedidoId];
    if (!bloqueId || this.procesando()) return;
    this.procesando.set(true);
    this.api.reagendar(pedidoId, bloqueId).subscribe({
      next: () => { this.procesando.set(false); this.toast.mostrar('Visita asociada al pedido.', 'exito'); this.cargar(); },
      error: err => { this.procesando.set(false); this.toast.mostrar(err.error?.mensaje ?? err.error?.error ?? 'No se pudo reagendar.', 'error'); this.cargar(); },
    });
  }

  revisar(pagoId: number): void {
    if (this.procesando()) return;
    this.procesando.set(true);
    this.api.revisar(pagoId).subscribe({
      next: resultado => {
        this.procesando.set(false);
        this.toast.mostrar(resultado.mensaje, 'exito');
        this.cargar();
      },
      error: err => {
        this.procesando.set(false);
        this.toast.mostrar(err.error?.mensaje ?? 'No se pudo consultar el pago en Webpay.', 'error');
        this.cargar();
      },
    });
  }
}
