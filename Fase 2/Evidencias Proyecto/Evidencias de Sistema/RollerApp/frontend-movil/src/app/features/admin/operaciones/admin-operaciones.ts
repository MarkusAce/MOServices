import { ObservacionesVisitaComponent } from '../../../shared/components/observaciones-visita/observaciones-visita';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Cotizacion } from '../../../core/models/cotizacion.model';
import { IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonSpinner, IonItem, IonLabel, IonSelect, IonSelectOption, IonSearchbar } from '@ionic/angular';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { Usuario } from '../../../core/models/usuario.model';
import { finalize } from 'rxjs';
import { DialogoMovilService } from '../../../core/services/dialogo-movil.service';

interface BloqueAdmin { id: number; fecha: string; horaInicio: string; horaFin: string; estado: string; tecnicoId: number | null; }
interface EventoAuditoria { id: number; actorId: number; accion: string; recurso: string; recursoId: number; detalle: string | null; creadoEn: string; }
interface Incidencia { pagoId: number; cotizacionId: number; pedidoId: number | null; monto: number; estado: string; motivo: string; creadoEn: string; }

@Component({
  selector: 'app-admin-operaciones',
  imports: [ObservacionesVisitaComponent, CurrencyPipe, DatePipe, IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonSpinner, IonItem, IonLabel, IonSelect, IonSelectOption, IonSearchbar, RouterLink],
  templateUrl: './admin-operaciones.html',
  styleUrl: './admin-operaciones.scss',
})
export class AdminOperacionesComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private dialogos = inject(DialogoMovilService);
  auth = inject(AuthService);
  readonly agenda = signal<BloqueAdmin[]>([]);
  readonly usuarios = signal<Usuario[]>([]);
  readonly buscarUsuario = signal('');
  readonly filtroRol = signal('TODOS');
  readonly usuariosVisibles = computed(() => {
    const q = this.buscarUsuario().trim().toLocaleLowerCase('es');
    return this.usuarios().filter(u => (this.filtroRol() === 'TODOS' || u.rol === this.filtroRol()) &&
      (!q || [String(u.id), u.nombre, u.apellido, u.correo].some(v => v.toLocaleLowerCase('es').includes(q))));
  });
  readonly incidencias = signal<Incidencia[]>([]);
  readonly filtroIncidencia = signal('TODOS');
  readonly estadosIncidencias = computed(() => [...new Set(this.incidencias().map(i => i.estado))].sort());
  readonly incidenciasVisibles = computed(() => this.incidencias().filter(i =>
    this.filtroIncidencia() === 'TODOS' || i.estado === this.filtroIncidencia()));
  readonly auditoria = signal<EventoAuditoria[]>([]);
  readonly paginaAuditoria = signal(0);
  readonly paginasAuditoria = signal(0);
  readonly totalAuditoria = signal(0);
  readonly cotizaciones = signal<Cotizacion[]>([]);
  readonly paginaCotizaciones = signal(0);
  readonly totalCotizaciones = signal(0);
  readonly paginasCotizaciones = signal(0);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly aviso = signal('');
  readonly seccion = signal<'agenda' | 'usuarios' | 'incidencias' | 'cotizaciones' | 'auditoria'>('agenda');
  readonly tecnicoPorBloque: Record<number, number> = {};
  ngOnInit(): void {
    const secciones = ['agenda', 'usuarios', 'incidencias', 'cotizaciones', 'auditoria'] as const;
    const solicitada = this.route.snapshot.queryParamMap.get('seccion');
    const inicial = this.route.snapshot.data['seccion'] === 'agenda' ? 'agenda' :
      secciones.find(s => s === solicitada) ?? 'usuarios';
    this.abrir(inicial);
    this.route.queryParamMap.subscribe(params => {
      if (this.route.snapshot.data['seccion'] === 'agenda') return;
      const nueva = secciones.find(s => s === params.get('seccion'));
      if (nueva && nueva !== this.seccion()) this.abrir(nueva);
    });
  }
  abrir(seccion: 'agenda' | 'usuarios' | 'incidencias' | 'cotizaciones' | 'auditoria'): void {
    this.seccion.set(seccion); this.error.set(''); this.aviso.set('');
    if (seccion === 'auditoria') { this.cargarAuditoria(0); return; }
    if (seccion === 'cotizaciones') { this.cargarCotizaciones(0); return; }
    this.cargando.set(true);
    const url = seccion === 'agenda' ? '/agenda/admin/bloques' : seccion === 'usuarios' ? '/auth/usuarios' : '/pagos/admin/incidencias';
    this.http.get<unknown>(environment.apiUrl + url).pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: datos => {
        if (seccion === 'agenda') this.agenda.set(datos as BloqueAdmin[]);
        if (seccion === 'usuarios') this.usuarios.set(datos as Usuario[]);
        if (seccion === 'incidencias') this.incidencias.set(datos as Incidencia[]);
      }, error: () => this.error.set('No se pudieron consultar los datos. Verifica tu sesión o inténtalo más tarde.'),
    });
  }
  cargarAuditoria(pagina: number): void {
    if (pagina < 0 || (this.paginasAuditoria() > 0 && pagina >= this.paginasAuditoria())) return;
    this.cargando.set(true); this.error.set('');
    this.http.get<{content: EventoAuditoria[]; number: number; totalElements: number; totalPages: number}>(
      environment.apiUrl + `/admin/auditoria?pagina=${pagina}&cantidad=20`)
      .pipe(finalize(() => this.cargando.set(false))).subscribe({
        next: datos => {
          this.auditoria.set(datos.content);
          this.paginaAuditoria.set(datos.number);
          this.paginasAuditoria.set(datos.totalPages);
          this.totalAuditoria.set(datos.totalElements);
        },
        error: () => this.error.set('No fue posible consultar la auditoría. Revisa tu sesión ADMIN y la migración SQL.'),
      });
  }
  cargarCotizaciones(pagina: number): void {
    if (pagina < 0 || (this.paginasCotizaciones() > 0 && pagina >= this.paginasCotizaciones())) return;
    this.cargando.set(true);
    this.error.set('');
    this.http.get<{content: Cotizacion[]; number: number; totalElements: number; totalPages: number}>(
      environment.apiUrl + `/cotizaciones/admin?pagina=${pagina}&cantidad=20`)
      .pipe(finalize(() => this.cargando.set(false))).subscribe({
        next: resultado => {
          this.cotizaciones.set(resultado.content);
          this.paginaCotizaciones.set(resultado.number);
          this.totalCotizaciones.set(resultado.totalElements);
          this.paginasCotizaciones.set(resultado.totalPages);
        },
        error: () => this.error.set('No fue posible consultar las cotizaciones. Comprueba tu sesión ADMIN y el backend.'),
      });
  }
  async asignar(bloque: BloqueAdmin): Promise<void> {
    const tecnicoId = Number(this.tecnicoPorBloque[bloque.id]);
    if (!tecnicoId || !this.usuarios().some(u => u.id === tecnicoId && u.rol === 'TECNICO' && u.activo)) {
      this.error.set('Selecciona un técnico activo.'); return;
    }
    const tecnico = this.usuarios().find(u => u.id === tecnicoId);
    if (!await this.dialogos.confirmar('Confirmar asignación', `¿Asignar a ${tecnico?.nombre} ${tecnico?.apellido} al bloque #${bloque.id} del ${bloque.fecha}?`, 'Asignar técnico')) return;
    this.guardando.set(true); this.error.set('');
    this.http.put(environment.apiUrl + `/agenda/admin/bloques/${bloque.id}/tecnico`, { tecnicoId })
      .pipe(finalize(() => this.guardando.set(false))).subscribe({
        next: () => { this.abrir('agenda'); this.aviso.set('Asignación guardada.'); void this.dialogos.informar('Técnico asignado correctamente.'); },
        error: () => this.error.set('No se pudo asignar el técnico. Comprueba las reglas del bloque.'),
      });
  }
  cargarTecnicos(): void {
    this.http.get<Usuario[]>(environment.apiUrl + '/auth/usuarios').subscribe({
      next: usuarios => this.usuarios.set(usuarios),
      error: () => this.error.set('No se pudo cargar la lista de técnicos.'),
    });
  }
  async cambiarEstado(usuario: Usuario): Promise<void> {
    if (usuario.id === this.auth.usuarioActual()?.id) { this.error.set('No puedes cambiar tu propia cuenta desde aquí.'); return; }
    if (!await this.dialogos.confirmar(usuario.activo ? 'Desactivar usuario' : 'Activar usuario', `¿${usuario.activo ? 'Desactivar' : 'Activar'} la cuenta de ${usuario.nombre} ${usuario.apellido}? ${usuario.activo ? 'No podrá iniciar sesión mientras esté desactivada.' : 'Podrá volver a iniciar sesión.'}`, usuario.activo ? 'Desactivar' : 'Activar', usuario.activo)) return;
    this.guardando.set(true); this.error.set('');
    this.auth.cambiarEstadoUsuario(usuario.id, !usuario.activo).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () => { this.abrir('usuarios'); this.aviso.set('Estado actualizado.'); },
      error: () => this.error.set('No se pudo modificar el usuario. Revisa las restricciones del servidor.'),
    });
  }
}
