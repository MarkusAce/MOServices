import { Subscription, combineLatest, map, distinctUntilChanged } from 'rxjs';
import { imagenProducto } from '../../core/utils/imagen-producto';
import { Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductoService } from '../../core/services/producto.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { CotizacionService } from '../../core/services/cotizacion.service';
import { DireccionesService, SugerenciaDireccion } from '../../core/services/direcciones.service';
import { AuthService } from '../../core/services/auth.service';
import { CarritoService } from '../../core/services/carrito.service';
import { ToastService } from '../../core/services/toast.service';
import { Producto, Tela, Mecanismo, ServicioAdicional } from '../../core/models/producto.model';

@Component({
  selector: 'app-producto-detalle',
  imports: [ReactiveFormsModule, CurrencyPipe, DecimalPipe],
  templateUrl: './producto-detalle.html',
  styleUrl: './producto-detalle.scss',
})
export class ProductoDetalleComponent implements OnInit, OnDestroy {
  readonly imagenProducto = imagenProducto;

  imagenFallback(evento: Event): void {
    const imagen = evento.target as HTMLImageElement;
    imagen.onerror = null;
    imagen.src = '/assets/img/cortina3.jpg';
  }

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private productoService = inject(ProductoService);
  private catalogoService = inject(CatalogoService);
  private cotizacionService = inject(CotizacionService);
  carrito = inject(CarritoService);
  editandoCarritoId = 0;
  private direccionesService = inject(DireccionesService);
  direccionesHabilitadas = signal(false);
  direccionManual = signal(false);
  referenciasDireccion = "";
  usarDireccionManual():void{this.direccionManual.set(true);this.lugarSeleccionado.set(null);this.sugerenciasDireccion.set([]);}
  sugerenciasDireccion = signal<SugerenciaDireccion[]>([]);
  buscandoDireccion = signal(false);
  lugarSeleccionado = signal<string | null>(null);
  private timerDireccion: ReturnType<typeof setTimeout> | null = null;
  private tokenDireccion = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}${Math.random().toString(36).slice(2)}`;

  buscarDireccion(texto: string): void {
    this.lugarSeleccionado.set(null);
    if (this.timerDireccion) clearTimeout(this.timerDireccion);
    if (this.direccionManual() || !this.direccionesHabilitadas() || texto.trim().length < 3) {
      this.sugerenciasDireccion.set([]); return;
    }
    this.buscandoDireccion.set(true);
    this.timerDireccion = setTimeout(() => {
      this.cargas.add(this.direccionesService.sugerencias(texto, this.tokenDireccion).subscribe({
        next: opciones => {
          if (this.form.controls.direccion.value === texto) this.sugerenciasDireccion.set(opciones);
          this.buscandoDireccion.set(false);
        },
        error: () => { this.sugerenciasDireccion.set([]); this.buscandoDireccion.set(false);
          this.toast.mostrar('No se pudieron buscar direcciones.', 'error'); },
      }));
    }, 350);
  }

  seleccionarDireccion(opcion: SugerenciaDireccion): void {
    this.cargas.add(this.direccionesService.detalle(opcion.placeId, this.tokenDireccion).subscribe({
      next: verificada => {
        this.form.patchValue({ direccion: verificada.direccion, comuna: verificada.comuna });
        this.lugarSeleccionado.set(verificada.placeId);
        this.sugerenciasDireccion.set([]);
        this.tokenDireccion = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}${Math.random().toString(36).slice(2)}`;
      },
      error: err => this.toast.mostrar(err.error?.mensaje ?? err.error?.error ?? 'Dirección fuera de cobertura.', 'error'),
    }));
  }
  private toast = inject(ToastService);
  auth = inject(AuthService);

  producto = signal<Producto | null>(null);
  imagenActiva = signal(0);
  galeriaProducto = computed(() => {
    const producto = this.producto();
    if (!producto?.imagenPrincipal) return [];
    const src = this.imagenProducto(producto.imagenPrincipal);
    return [
      { src, etiqueta: 'Vista frontal', clase: 'vista-frontal' },
      { src, etiqueta: 'Vista lateral', clase: 'vista-lateral' },
      { src, etiqueta: 'Detalle del mecanismo', clase: 'vista-detalle' },
    ];
  });

  seleccionarImagen(indice: number): void { this.imagenActiva.set(indice); }
  imagenAnterior(): void { const total = this.galeriaProducto().length; if (total) this.imagenActiva.set((this.imagenActiva() - 1 + total) % total); }
  imagenSiguiente(): void { const total = this.galeriaProducto().length; if (total) this.imagenActiva.set((this.imagenActiva() + 1) % total); }
  telas = signal<Tela[]>([]);
  mecanismos = signal<Mecanismo[]>([]);
  servicios = signal<ServicioAdicional[]>([]);
  enviando = signal(false);
  errorCarga = signal('');
  cargandoProducto = signal(true);

  form = this.fb.nonNullable.group({
    telaId: [0, [Validators.required, Validators.min(1)]],
    mecanismoId: [0, [Validators.required, Validators.min(1)]],
    anchoCm: [100, [Validators.required, Validators.min(1)]],
    altoCm: [160, [Validators.required, Validators.min(1)]],
    direccion: ['', Validators.required],
    comuna: ['', Validators.required],
    servicioIds: this.fb.nonNullable.control<number[]>([]),
  });

  private valoresFormulario = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  previsualizacion = computed(() => {
    this.valoresFormulario();
    const valores = this.form.getRawValue();
    const tela = this.telas().find((t) => t.id === Number(valores.telaId));
    const mecanismo = this.mecanismos().find((m) => m.id === Number(valores.mecanismoId));

    const producto = this.producto();
    if (!producto || !tela || !mecanismo || this.form.invalid ||
        !Number.isFinite(Number(valores.anchoCm)) || !Number.isFinite(Number(valores.altoCm)) ||
        Number(valores.anchoCm) < 1 || Number(valores.altoCm) < 1 ||
        Number(valores.anchoCm) > producto.anchoMaxCm || Number(valores.altoCm) > producto.altoMaxCm) return null;

    const metrosCuadrados = (valores.anchoCm / 100) * (valores.altoCm / 100);
    const metrosCuadradosFacturados = Math.max(1, metrosCuadrados);
    const aplicaCobroMinimo = metrosCuadrados < 1;
    const valorTela = metrosCuadradosFacturados * tela.precioM2;
    const valorMecanismo = mecanismo.valorFijo;
    const valorServicios = this.servicios()
      .filter((s) => valores.servicioIds.includes(s.id))
      .reduce((acc, s) => acc + s.precio, 0);

    return {
      metrosCuadrados,
      metrosCuadradosFacturados,
      aplicaCobroMinimo,
      valorTela,
      valorMecanismo,
      valorServicios,
      total: valorTela + valorMecanismo + valorServicios,
    };
  });

  private parametros?: Subscription;
  private cargas = new Subscription();
  ngOnDestroy(): void { this.parametros?.unsubscribe(); this.cargas.unsubscribe(); if (this.timerDireccion) clearTimeout(this.timerDireccion); }
  ngOnInit(): void {
    this.parametros = combineLatest([this.route.paramMap, this.route.queryParamMap]).pipe(
      map(([params, query]) => [Number(params.get('id')), Number(query.get('carritoId'))]),
      distinctUntilChanged((anterior, actual) => anterior[0] === actual[0] && anterior[1] === actual[1]),
    ).subscribe(([id, carritoId]) => this.cargarProducto(id, carritoId));
  }
  private cargarProducto(id: number, carritoId: number): void {
    if (this.timerDireccion) clearTimeout(this.timerDireccion);
    this.referenciasDireccion = '';
    this.cargas.unsubscribe(); this.cargas = new Subscription();
    this.producto.set(null); this.imagenActiva.set(0); this.telas.set([]); this.mecanismos.set([]); this.servicios.set([]);
    this.errorCarga.set(''); this.cargandoProducto.set(true); this.enviando.set(false);
    this.editandoCarritoId = 0; this.direccionManual.set(false); this.lugarSeleccionado.set(null); this.sugerenciasDireccion.set([]);
    this.form.reset({telaId:0,mecanismoId:0,anchoCm:100,altoCm:160,direccion:'',comuna:'',servicioIds:[]});
    this.cargas.add(this.direccionesService.configuracion().subscribe({
      next: estado => this.direccionesHabilitadas.set(estado.habilitado),
      error: () => this.direccionesHabilitadas.set(false),
    }));
    if (Number.isSafeInteger(carritoId) && carritoId > 0 && this.carrito.contiene(carritoId)) {
      this.cargas.add(this.cotizacionService.obtenerPorId(carritoId).subscribe({
        next: c => {
          if (c.productoId !== id || c.estado === 'PAGADA' || c.estado === 'CANCELADA') return;
          this.editandoCarritoId = c.id;
          this.form.patchValue({telaId:c.telaId,mecanismoId:c.mecanismoId,anchoCm:c.anchoCm,altoCm:c.altoCm,
            direccion:c.direccion,comuna:c.comuna,servicioIds:(c.servicios ?? []).map(s => s.id)});
        },
        error:() => this.toast.mostrar('No se pudo recuperar la configuración del carrito.','error'),
      }));
    }

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.errorCarga.set('El producto solicitado no es válido.');
      this.cargandoProducto.set(false);
      return;
    }
    this.cargas.add(this.productoService.obtenerPorId(id).subscribe({
      next: (producto) => {
        this.producto.set(producto);
        this.cargandoProducto.set(false);
        this.seleccionarValoresDisponibles();
      },
      error: () => {
        this.errorCarga.set('No se pudo cargar el producto. Vuelve al catálogo e inténtalo de nuevo.');
        this.cargandoProducto.set(false);
      },
    }));

    this.cargas.add(this.catalogoService.listarTelas().subscribe({next: (telas) => {
      this.telas.set(telas);
      this.seleccionarValoresDisponibles();
    }, error: () => this.errorCarga.set('No se pudieron cargar los telas. Vuelve a intentarlo.')}));
    this.cargas.add(this.catalogoService.listarMecanismos().subscribe({next: (mecanismos) => {
      this.mecanismos.set(mecanismos);
      this.seleccionarValoresDisponibles();
    }, error: () => this.errorCarga.set('No se pudieron cargar los mecanismos. Vuelve a intentarlo.')}));
    this.cargas.add(this.catalogoService.listarServicios().subscribe({next: servicios => this.servicios.set(servicios), error: () => this.errorCarga.set('No se pudieron cargar los servicios. Vuelve a intentarlo.')}));
  }

  private seleccionarValoresDisponibles(): void {
    const producto = this.producto();
    if (!producto) return;
    const telas = this.telas();
    const mecanismos = this.mecanismos();
    const telaActual = Number(this.form.controls.telaId.value);
    const mecanismoActual = Number(this.form.controls.mecanismoId.value);
    if (telas.length && !telas.some(t => t.id === telaActual)) {
      const defecto = telas.find(t => t.id === producto.telaDefectoId);
      this.form.controls.telaId.setValue((defecto ?? telas[0]).id);
    }
    if (mecanismos.length && !mecanismos.some(m => m.id === mecanismoActual)) {
      const defecto = mecanismos.find(m => m.id === producto.mecanismoDefectoId);
      this.form.controls.mecanismoId.setValue((defecto ?? mecanismos[0]).id);
    }
  }

  alternarServicio(servicioId: number, marcado: boolean): void {
    const actuales = this.form.controls.servicioIds.value;
    this.form.controls.servicioIds.setValue(
      marcado ? [...new Set([...actuales, servicioId])] : actuales.filter((id) => id !== servicioId)
    );
  }

  errorCobertura = signal('');

  cotizar(alCarrito = false): void {
    alCarrito = alCarrito || !!this.editandoCarritoId;
    if (alCarrito && !this.editandoCarritoId && this.carrito.cantidad() >= 20) {
      this.toast.mostrar('El carrito admite hasta 20 cortinas. Quita alguna antes de añadir otra.','error'); return;
    }
    this.errorCobertura.set('');
    if (!this.auth.estaLogeado()) {
      this.toast.mostrar('Inicia sesión para poder cotizar.', 'error');
      this.router.navigateByUrl('/login');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.mostrar('Completa todos los campos requeridos.', 'error');
      return;
    }

    const producto = this.producto();
    if (!producto || this.enviando()) return;
    if (!this.previsualizacion()) {
      this.toast.mostrar('Revisa las medidas y selecciona una tela y un mecanismo disponibles.', 'error');
      return;
    }

    this.enviando.set(true);
    const valores = this.form.getRawValue();

    this.cotizacionService.crear({
      productoId: producto.id,
      telaId: Number(valores.telaId),
      mecanismoId: Number(valores.mecanismoId),
      anchoCm: valores.anchoCm,
      altoCm: valores.altoCm,
      direccion: valores.direccion.trim(),
      comuna: valores.comuna.trim(),
      servicioIds: valores.servicioIds,
      placeId: this.direccionManual() ? null : this.lugarSeleccionado(),
      direccionManual: this.direccionManual() || !this.direccionesHabilitadas(),
      referenciasDireccion: this.referenciasDireccion,
    }).subscribe({
      next: (cotizacion) => {
        this.toast.mostrar(cotizacion.mensajeCobroMinimo ?? 'Cotización generada correctamente.', 'exito');
        if (alCarrito) {
          this.carrito.agregar(cotizacion,this.editandoCarritoId || undefined);
          this.router.navigateByUrl('/carrito');
        } else {
          this.router.navigate(['/agenda'], { queryParams: { cotizacionId: cotizacion.id } });
        }
      },
      error: (error) => {
        const mensaje = error.error?.mensaje ?? error.error?.error ?? 'No se pudo generar la cotización.';
        if (error.status === 400 && mensaje.includes('visitas técnicas')) this.errorCobertura.set(mensaje);
        this.toast.mostrar(mensaje, 'error');
        this.enviando.set(false);
      },
    });
  }
}
