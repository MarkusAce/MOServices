import { ObservacionesVisitaComponent } from '../../../shared/components/observaciones-visita/observaciones-visita';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AgendaAdminBloque, AgendaService } from '../../../core/services/agenda.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Usuario } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-admin-agenda',
  imports: [ObservacionesVisitaComponent, FormsModule],
  templateUrl: './admin-agenda.html',
  styleUrl: './admin-agenda.scss',
})
export class AdminAgendaComponent implements OnInit {
  private agenda = inject(AgendaService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  bloques = signal<AgendaAdminBloque[]>([]);
  tecnicos = signal<Usuario[]>([]);
  cargando = signal(true);
  procesando = signal(false);
  fecha = '';
  horaInicio = '10:00';
  horaFin = '11:00';
  tecnicoId: number | null = null;

  ngOnInit(): void {
    this.cargar();
    this.auth.listarUsuarios().subscribe({
      next: usuarios => this.tecnicos.set(usuarios.filter(u => u.rol === 'TECNICO' && u.activo)),
      error: () => this.toast.mostrar('No se pudieron cargar los técnicos.', 'error'),
    });
  }

  cargar(): void {
    this.agenda.listarAdmin().subscribe({
      next: bloques => { this.bloques.set(bloques); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.toast.mostrar('No se pudo cargar la agenda.', 'error'); },
    });
  }

  crear(): void {
    if (!this.fecha || !this.horaInicio || !this.horaFin || !this.tecnicoId || this.procesando()) return;
    this.procesando.set(true);
    this.agenda.crear({ fecha: this.fecha, horaInicio: this.horaInicio,
      horaFin: this.horaFin, tecnicoId: this.tecnicoId }).subscribe({
      next: () => { this.procesando.set(false); this.toast.mostrar('Horario creado.', 'exito'); this.cargar(); },
      error: err => this.mostrarError(err, 'No se pudo crear el horario.'),
    });
  }

  iniciarAtencion(id: number): void {
    if (this.procesando()) return;
    this.procesando.set(true);
    this.agenda.iniciar(id).subscribe({next: () => { this.procesando.set(false); this.cargar(); }, error: e => this.mostrarError(e, 'No se pudo iniciar la atención.')});
  }

  completar(id: number): void {
    if (this.procesando()) return;
    this.procesando.set(true);
    this.agenda.completar(id).subscribe({
      next: () => { this.procesando.set(false); this.toast.mostrar('Visita completada.', 'exito'); this.cargar(); },
      error: err => this.mostrarError(err, 'No se pudo completar la visita.')
    });
  }

  cancelar(id: number): void {
    if (this.procesando()) return;
    this.procesando.set(true);
    this.agenda.cancelar(id).subscribe({
      next: () => { this.procesando.set(false); this.toast.mostrar('Horario cancelado.', 'exito'); this.cargar(); },
      error: err => this.mostrarError(err, 'No se pudo cancelar el horario.'),
    });
  }

  asignar(id: number, tecnicoId: number | null): void {
    if (!tecnicoId || this.procesando()) return;
    this.procesando.set(true);
    this.agenda.asignarTecnico(id, tecnicoId).subscribe({
      next: () => { this.procesando.set(false); this.toast.mostrar('Técnico asignado.', 'exito'); this.cargar(); },
      error: err => { this.mostrarError(err, 'No se pudo asignar el técnico.'); this.cargar(); },
    });
  }

  nombreTecnico(id?: number | null): string {
    const tecnico = this.tecnicos().find(u => u.id === id);
    return tecnico ? `${tecnico.nombre} ${tecnico.apellido}` : 'Sin técnico';
  }

  private mostrarError(err: any, mensaje: string): void {
    this.procesando.set(false);
    this.toast.mostrar(err.error?.error ?? err.error?.mensaje ?? mensaje, 'error');
  }
}
