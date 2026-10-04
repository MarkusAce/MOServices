import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { ViewChild, Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { switchMap, of, finalize } from 'rxjs';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Categoria, Producto, Tela, Mecanismo } from '../../../core/models/producto.model';
import { ProductoService } from '../../../core/services/producto.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { ToastService } from '../../../core/services/toast.service';
import { imagenProducto as resolverImagenProducto } from '../../../core/utils/imagen-producto';

@Component({ selector: 'app-admin-productos', imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, ReactiveFormsModule], templateUrl: './admin-productos.html', styleUrl: './admin-productos.scss' })
export class AdminProductosComponent implements OnDestroy {
  @ViewChild(IonContent) contenido?: IonContent;

  private api = inject(ProductoService);
  private catalogo = inject(CatalogoService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  productos = signal<Producto[]>([]);
  telas = signal<Tela[]>([]);
  mecanismos = signal<Mecanismo[]>([]);
  mostrandoFormulario = signal(false);
  editandoId = signal<number | null>(null);
  guardando = signal(false);
  cargando = signal(false);
  errorCarga = signal('');
  confirmarArchivo = signal<number | null>(null);
  archivoImagen = signal<File | null>(null);
  vistaPrevia = signal<string | null>(null);
  errorImagen = signal('');
  imagenProducto(ruta: string | null): string { return resolverImagenProducto(ruta); }
  imagenFallback(evento: Event): void { (evento.target as HTMLImageElement).src = 'assets/img/roller-clasica.svg'; }
  categorias: Categoria[] = ['BLACKOUT', 'CLASICA', 'MODERNA', 'PANEL'];
  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
    descripcion: ['', [Validators.required, Validators.pattern(/\S/)]],
    categoria: ['BLACKOUT' as Categoria, Validators.required], imagenPrincipal: [''],
    telaDefectoId: [0], mecanismoDefectoId: [0],
    anchoMaxCm: [200, [Validators.required, Validators.min(1), Validators.max(9999.99)]],
    altoMaxCm: [260, [Validators.required, Validators.min(1), Validators.max(9999.99)]],
  });
  ionViewWillEnter(): void {
    this.cargar();
    this.catalogo.listarTodasTelas().subscribe({next: t => this.telas.set(t), error: () => this.toast.mostrar('No se pudieron cargar las telas predeterminadas.', 'error')});
    this.catalogo.listarTodosMecanismos().subscribe({next: m => this.mecanismos.set(m), error: () => this.toast.mostrar('No se pudieron cargar los mecanismos predeterminados.', 'error')});
  }
  ngOnDestroy(): void { this.limpiarImagen(); }
  private limpiarImagen(): void {
    const url = this.vistaPrevia(); if (url) URL.revokeObjectURL(url);
    this.vistaPrevia.set(null); this.archivoImagen.set(null); this.errorImagen.set('');
  }
  seleccionarImagen(evento: Event): void {
    this.limpiarImagen();
    const archivo = (evento.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    if (!['image/jpeg','image/png'].includes(archivo.type) || archivo.size > 5 * 1024 * 1024) {
      this.errorImagen.set('Selecciona una imagen JPG o PNG de hasta 5 MB.'); return;
    }
    this.archivoImagen.set(archivo); this.vistaPrevia.set(URL.createObjectURL(archivo));
  }
  nuevo(): void {
    if (this.guardando()) return;
    this.cancelar(); this.mostrandoFormulario.set(true);
  }
  cancelar(): void {
    if (this.guardando()) return;
    this.editandoId.set(null); this.mostrandoFormulario.set(false); this.limpiarImagen();
    this.form.reset({nombre:'',descripcion:'',categoria:'BLACKOUT',imagenPrincipal:'',telaDefectoId:0,mecanismoDefectoId:0,anchoMaxCm:200,altoMaxCm:260});
  }
  editar(p: Producto): void {
    if (this.guardando()) return;
    this.limpiarImagen(); this.confirmarArchivo.set(null); this.editandoId.set(p.id);
    this.form.reset({...p, imagenPrincipal:p.imagenPrincipal ?? '',telaDefectoId:p.telaDefectoId ?? 0,mecanismoDefectoId:p.mecanismoDefectoId ?? 0});
    this.mostrandoFormulario.set(true);
    void this.contenido?.scrollToTop(250);
  }
  guardarProducto(): void {
    if (this.form.invalid || this.guardando() || this.errorImagen()) { this.form.markAllAsTouched(); return; }
    this.guardando.set(true);
    const valores = this.form.getRawValue(); const id = this.editandoId(); const archivo = this.archivoImagen();
    const imagen$ = archivo ? this.api.subirImagen(archivo) : of({imagenPrincipal:valores.imagenPrincipal.trim() || null});
    imagen$.pipe(switchMap(imagen => {
      const datos = {...valores,nombre:valores.nombre.trim(),descripcion:valores.descripcion.trim(),imagenPrincipal:imagen.imagenPrincipal,
        telaDefectoId:Number(valores.telaDefectoId) || null,mecanismoDefectoId:Number(valores.mecanismoDefectoId) || null};
      return id === null ? this.api.crear(datos) : this.api.editar(id,datos);
    }), finalize(() => this.guardando.set(false))).subscribe({
      next: () => { this.guardando.set(false); this.cancelar(); this.toast.mostrar(id === null ? 'Producto creado.' : 'Producto actualizado.', 'exito'); this.cargar(); },
      error: e => this.toast.mostrar(e.error?.detail ?? e.error?.error ?? 'No se pudo guardar el producto.', 'error'),
    });
  }
  cargar(): void {
    this.cargando.set(true); this.errorCarga.set('');
    this.api.listarTodos().pipe(finalize(() => this.cargando.set(false))).subscribe({next:p => this.productos.set(p),error:() => this.errorCarga.set('No se pudieron cargar los productos.')});
  }
  alternarActivo(p: Producto): void {
    if (this.guardando()) return;
    this.guardando.set(true);
    const accion = p.activo ? this.api.archivar(p.id) : this.api.actualizarEstado(p.id,true);
    accion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: actualizado => { this.productos.update(lista => lista.map(x => x.id === p.id ? actualizado : x)); this.confirmarArchivo.set(null); this.toast.mostrar(p.activo ? 'Producto archivado.' : 'Producto restaurado.', 'exito'); },
      error:e => this.toast.mostrar(e.error?.error ?? 'No se pudo cambiar el estado.', 'error'),
    });
  }
}
