import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { finalize } from 'rxjs';
import { GestionServiciosService, DatosServicio } from '../../../core/services/gestion-servicios.service';
import { ServicioAdicional } from '../../../core/models/producto.model';
@Component({ selector: 'app-gestion-servicios', standalone: true, imports: [FormsModule, CurrencyPipe], templateUrl: './gestion-servicios.html', styleUrl: './gestion-servicios.scss' })
export class GestionServiciosComponent implements OnInit {
 private api = inject(GestionServiciosService);
 servicios = signal<ServicioAdicional[]>([]); cargando = signal(false); guardando = signal(false);
 error = signal(''); mensaje = signal(''); id: number | null = null; busqueda = ''; estado = 'todos';
 datos: DatosServicio = this.vacios();
 private vacios(): DatosServicio { return { nombre: '', descripcion: '', precio: 0, comisionTecnico: 0, activo: true }; }
 ngOnInit() { this.cargar(); }
 cargar() { this.cargando.set(true); this.error.set(''); this.api.listar().pipe(finalize(() => this.cargando.set(false))).subscribe({next: s => this.servicios.set(s), error: () => this.error.set('No se pudieron cargar los servicios. Intenta nuevamente.')}); }
 visibles() { return this.servicios().filter(s => s.nombre.toLowerCase().includes(this.busqueda.trim().toLowerCase()) && (this.estado === 'todos' || s.activo === (this.estado === 'activos'))); }
 editar(s: ServicioAdicional) { this.id = s.id; this.datos = {nombre: s.nombre, descripcion: s.descripcion, precio: s.precio, comisionTecnico: s.comisionTecnico, activo: s.activo}; this.mensaje.set(''); }
 cancelar() { this.id = null; this.datos = this.vacios(); }
 guardar() {
  if (this.guardando()) return;
  this.guardando.set(true); this.error.set(''); this.mensaje.set('');
  this.api.guardar(this.id, this.datos).pipe(finalize(() => this.guardando.set(false))).subscribe({next: s => { this.reemplazar(s); this.cancelar(); this.mensaje.set('Servicio guardado.'); }, error: () => this.error.set('No se pudo guardar. Revisa los datos e intenta nuevamente.')});
 }
 cambiar(s: ServicioAdicional) {
  if (this.guardando()) return;
  this.guardando.set(true); this.error.set(''); this.mensaje.set('');
  this.api.disponibilidad(s.id, !s.activo).pipe(finalize(() => this.guardando.set(false))).subscribe({next: actualizado => {this.reemplazar(actualizado); this.mensaje.set(actualizado.activo ? 'Servicio activado.' : 'Servicio desactivado.');}, error: () => this.error.set('No se pudo cambiar la disponibilidad.')});
 }
 private reemplazar(s: ServicioAdicional) { this.servicios.update(lista => [...lista.filter(x => x.id !== s.id), s].sort((a,b) => a.nombre.localeCompare(b.nombre))); }
}
