import { DialogoMovilService } from '../../../core/services/dialogo-movil.service';
import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComisionService } from '../../../core/services/comision.service';
import { ToastService } from '../../../core/services/toast.service';
import { Comision, EstadoComision } from '../../../core/models/comision.model';

@Component({
  selector: 'app-admin-comisiones',
  imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, CurrencyPipe, DatePipe, FormsModule],
  templateUrl: './admin-comisiones.html',
  styleUrl: './admin-comisiones.scss',
})
export class AdminComisionesComponent  {
  private dialogos = inject(DialogoMovilService);
  private servicio = inject(ComisionService);
  private toast = inject(ToastService);
  comisiones = signal<Comision[]>([]);
  cargando = signal(false);
  procesando = signal<number | null>(null);
  error = signal('');
  estado: '' | EstadoComision = '';
  usuarioId: number | null = null;

  ionViewWillEnter(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.servicio.listar(this.estado || undefined, this.usuarioId ?? undefined).subscribe({
      next: datos => { this.comisiones.set(datos); this.cargando.set(false); },
      error: () => { this.error.set('No se pudieron cargar las comisiones.'); this.cargando.set(false); },
    });
  }

  async liquidar(comision: Comision): Promise<void> {
    if (comision.estado !== 'LIQUIDABLE' || this.procesando() != null) return;
    if (!await this.dialogos.confirmar('Liquidar comisión', `¿Confirmar pago de la comisión #${comision.id}?`, 'Marcar pagada')) return;
    if (this.procesando() !== null) return;
    this.procesando.set(comision.id);
    this.servicio.liquidar(comision.id).subscribe({
      next: () => { this.procesando.set(null); this.toast.mostrar('Comisión marcada como pagada.'); this.cargar(); },
      error: (error) => {
        this.procesando.set(null);
        this.toast.mostrar(error.error?.error ?? 'No se pudo liquidar la comisión.', 'error');
      },
    });
  }
}
