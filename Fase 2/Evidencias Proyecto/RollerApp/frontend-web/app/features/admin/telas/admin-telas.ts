import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { ToastService } from '../../../core/services/toast.service';
import { Tela, PasoLuz } from '../../../core/models/producto.model';

@Component({ selector:'app-admin-telas', imports:[CurrencyPipe,FormsModule,RouterLink], templateUrl:'./admin-telas.html',styleUrl:'./admin-telas.scss' })
export class AdminTelasComponent implements OnInit {
  private api = inject(CatalogoService);
  private toast = inject(ToastService);
  telas = signal<Tela[]>([]);
  cargando = signal(false); guardando = signal(false); errorCarga = signal('');
  mostrandoFormulario = signal(false); editandoId: number | null = null;
  confirmarArchivo = signal<number | null>(null);
  busqueda = ''; estado = 'todos';
  datos = this.vacios();
  nivelesPasoLuz: {valor:PasoLuz;texto:string}[] = [{valor:'OPACO',texto:'Opaco'},{valor:'FILTRANTE',texto:'Filtrante'},{valor:'TRANSLUCIDO',texto:'Translúcido'},{valor:'REGULABLE',texto:'Regulable'}];
  private vacios(): Omit<Tela,'id'> { return {nombre:'',descripcion:'',precioM2:0,pasoLuz:'OPACO',activo:true}; }
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true); this.errorCarga.set('');
    this.api.listarTodasTelas().pipe(finalize(() => this.cargando.set(false))).subscribe({next:t => this.telas.set(t), error:() => this.errorCarga.set('No se pudieron cargar las telas.')});
  }
  visibles(): Tela[] { return this.telas().filter(t => t.nombre.toLowerCase().includes(this.busqueda.trim().toLowerCase()) && (this.estado === 'todos' || t.activo === (this.estado === 'activos'))); }
  nuevo(): void { if (this.guardando()) return; this.editandoId=null; this.datos=this.vacios(); this.mostrandoFormulario.set(true); }
  editar(t: Tela): void { if (this.guardando()) return; this.editandoId=t.id; this.datos={...t}; this.confirmarArchivo.set(null); this.mostrandoFormulario.set(true); window.scrollTo({top:0,behavior:'smooth'}); }
  cancelar(): void { if (this.guardando()) return; this.mostrandoFormulario.set(false); this.editandoId=null; this.datos=this.vacios(); }
  guardar(): void {
    if (this.guardando() || !this.datos.nombre.trim() || !Number.isFinite(this.datos.precioM2) || this.datos.precioM2 < 0) return;
    this.guardando.set(true); const id=this.editandoId;
    this.api.guardarTela(id,{...this.datos,nombre:this.datos.nombre.trim()}).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next:t => { this.reemplazar(t); this.guardando.set(false); this.cancelar(); this.toast.mostrar(id === null ? 'Tela creada.' : 'Tela actualizada.','exito'); },
      error:e => this.toast.mostrar(e.error?.detail ?? e.error?.error ?? 'No se pudo guardar la tela.','error'),
    });
  }
  alternar(t: Tela): void {
    if (this.guardando()) return;
    this.guardando.set(true);
    const accion = t.activo ? this.api.archivarTela(t.id) : this.api.guardarTela(t.id,{...t,activo:true});
    accion.pipe(finalize(() => this.guardando.set(false))).subscribe({next:actualizada => {this.reemplazar(actualizada);this.confirmarArchivo.set(null);this.toast.mostrar(t.activo ? 'Tela archivada.' : 'Tela restaurada.','exito');},error:e => this.toast.mostrar(e.error?.error ?? 'No se pudo cambiar el estado.','error')});
  }
  private reemplazar(t: Tela): void { this.telas.update(lista => [...lista.filter(x => x.id !== t.id),t].sort((a,b) => a.nombre.localeCompare(b.nombre))); }
}
