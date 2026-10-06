import { Component, OnInit, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { DatePipe } from '@angular/common';
import { PostventaService } from '../../../core/services/postventa.service';
import { ToastService } from '../../../core/services/toast.service';
import { PostventaSolicitud } from '../../../core/models/postventa.model';

@Component({
  selector: 'app-admin-postventa',
  imports: [DatePipe],
  templateUrl: './admin-postventa.html',
  styleUrl: './admin-postventa.scss',
})
export class AdminPostventaComponent implements OnInit {
  private postventaService = inject(PostventaService);
  private toast = inject(ToastService);

  procesando = signal(false);
  solicitudes = signal<PostventaSolicitud[]>([]);errorCarga=signal('');cargando=signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);this.errorCarga.set('');
    this.postventaService.listarTodas().subscribe({next: s => {this.solicitudes.set(s);this.cargando.set(false);}, error: () => {this.cargando.set(false);this.errorCarga.set('No se pudieron cargar las solicitudes.');}});
  }

  marcarResuelta(solicitud: PostventaSolicitud): void {
    if (this.procesando() || !confirm('¿Marcar esta solicitud como resuelta?')) return;
    this.procesando.set(true);
    this.postventaService.cambiarEstado(solicitud.id, 'RESUELTA').pipe(finalize(() => this.procesando.set(false))).subscribe({
      next: () => {
        this.toast.mostrar('Solicitud marcada como resuelta.', 'exito');
        this.cargar();
      },
      error: (error) => this.toast.mostrar(error.error?.mensaje ?? error.error?.error ?? 'No se pudo actualizar.', 'error'),
    });
  }

  eliminar(solicitud: PostventaSolicitud): void {
    if (this.procesando() || !confirm('¿Eliminar esta solicitud?')) return;
    this.procesando.set(true);
    this.postventaService.eliminar(solicitud.id).pipe(finalize(() => this.procesando.set(false))).subscribe({
      next: () => {
        this.toast.mostrar('Solicitud eliminada.', 'exito');
        this.cargar();
      },
      error: (error) => this.toast.mostrar(error.error?.mensaje ?? error.error?.error ?? 'No se pudo eliminar.', 'error'),
    });
  }
}
