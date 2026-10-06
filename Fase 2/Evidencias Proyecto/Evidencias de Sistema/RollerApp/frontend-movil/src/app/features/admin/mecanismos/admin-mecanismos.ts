import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { ViewChild, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { ToastService } from '../../../core/services/toast.service';
import { Mecanismo } from '../../../core/models/producto.model';

@Component({ selector:'app-admin-mecanismos', imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, CurrencyPipe,FormsModule,RouterLink], templateUrl:'./admin-mecanismos.html',styleUrl:'./admin-mecanismos.scss' })
export class AdminMecanismosComponent  {
  @ViewChild(IonContent) contenido?: IonContent;

  private api = inject(CatalogoService);
  private toast = inject(ToastService);
  mecanismos = signal<Mecanismo[]>([]);
  cargando = signal(false); guardando = signal(false); errorCarga = signal('');
  mostrandoFormulario = signal(false); editandoId: number | null = null;
  confirmarArchivo = signal<number | null>(null);
  busqueda = ''; estado = 'todos';
  datos = this.vacios();
  private vacios(): Omit<Mecanismo,'id'> { return {nombre:'',descripcion:'',valorFijo:0,activo:true}; }
  ionViewWillEnter(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true); this.errorCarga.set('');
    this.api.listarTodosMecanismos().pipe(finalize(() => this.cargando.set(false))).subscribe({next:t => this.mecanismos.set(t), error:() => this.errorCarga.set('No se pudieron cargar los mecanismos.')});
  }
  visibles(): Mecanismo[] { return this.mecanismos().filter(t => t.nombre.toLowerCase().includes(this.busqueda.trim().toLowerCase()) && (this.estado === 'todos' || t.activo === (this.estado === 'activos'))); }
  nuevo(): void { if (this.guardando()) return; this.editandoId=null; this.datos=this.vacios(); this.mostrandoFormulario.set(true); }
  editar(t: Mecanismo): void { if (this.guardando()) return; this.editandoId=t.id; this.datos={...t}; this.confirmarArchivo.set(null); this.mostrandoFormulario.set(true); void this.contenido?.scrollToTop(250); }
  cancelar(): void { if (this.guardando()) return; this.mostrandoFormulario.set(false); this.editandoId=null; this.datos=this.vacios(); }
  guardar(): void {
    if (this.guardando() || !this.datos.nombre.trim() || !Number.isFinite(this.datos.valorFijo) || this.datos.valorFijo < 0) return;
    this.guardando.set(true); const id=this.editandoId;
    this.api.guardarMecanismo(id,{...this.datos,nombre:this.datos.nombre.trim()}).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next:t => { this.reemplazar(t); this.guardando.set(false); this.cancelar(); this.toast.mostrar(id === null ? 'Mecanismo creado.' : 'Mecanismo actualizado.','exito'); },
      error:e => this.toast.mostrar(e.error?.detail ?? e.error?.error ?? 'No se pudo guardar el mecanismo.','error'),
    });
  }
  alternar(t: Mecanismo): void {
    if (this.guardando()) return;
    this.guardando.set(true);
    const accion = t.activo ? this.api.archivarMecanismo(t.id) : this.api.guardarMecanismo(t.id,{...t,activo:true});
    accion.pipe(finalize(() => this.guardando.set(false))).subscribe({next:actualizada => {this.reemplazar(actualizada);this.confirmarArchivo.set(null);this.toast.mostrar(t.activo ? 'Mecanismo desactivado.' : 'Mecanismo activado.','exito');},error:e => this.toast.mostrar(e.error?.error ?? 'No se pudo cambiar el estado.','error')});
  }
  private reemplazar(t: Mecanismo): void { this.mecanismos.update(lista => [...lista.filter(x => x.id !== t.id),t].sort((a,b) => a.nombre.localeCompare(b.nombre))); }
}
