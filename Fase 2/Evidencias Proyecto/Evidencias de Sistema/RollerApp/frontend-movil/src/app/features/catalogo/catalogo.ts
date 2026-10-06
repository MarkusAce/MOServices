import { imagenProducto } from '../../core/utils/imagen-producto';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonCard, IonCardHeader, IonCardTitle,
  IonCardContent, IonImg, IonSpinner, IonBadge,
} from '@ionic/angular';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../core/services/catalogo.service';
import { Mecanismo, Tela } from '../../core/models/producto.model';
import { ProductoService } from '../../core/services/producto.service';
import { Producto } from '../../core/models/producto.model';

@Component({
  selector: 'app-catalogo',
  imports: [
    RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonCard, IonCardHeader, IonCardTitle,
    IonCardContent, IonImg, IonSpinner, IonBadge,
  ],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.scss',
})
export class CatalogoComponent  {
  readonly imagenProducto = imagenProducto;
  private productoService = inject(ProductoService);
  private catalogoService = inject(CatalogoService);

  productos = signal<Producto[]>([]);
  cargando = signal(true);
  errorCarga = signal(false);
  telas = signal<Tela[]>([]);
  mecanismos = signal<Mecanismo[]>([]);
  telaId = signal(0);
  mecanismoId = signal(0);
  pasoLuz = signal('TODOS');
  nivelesPasoLuz = [
    { valor: 'OPACO', texto: 'Opaco' },
    { valor: 'FILTRANTE', texto: 'Filtrante' },
    { valor: 'TRANSLUCIDO', texto: 'Translúcido' },
    { valor: 'REGULABLE', texto: 'Regulable' },
  ];
  busqueda = signal('');
  categoria = signal('TODAS');
  categorias = computed(() => [...new Set(this.productos().map(p => p.categoria))]);
  productosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLocaleLowerCase('es-CL');
    return this.productos().filter(p =>
      (this.categoria() === 'TODAS' || p.categoria === this.categoria()) &&
      (this.pasoLuz() === 'TODOS' || this.telas().some(t => t.id === p.telaDefectoId && t.pasoLuz === this.pasoLuz())) &&
      (this.telaId() === 0 || p.telaDefectoId === this.telaId()) &&
      (this.mecanismoId() === 0 || p.mecanismoDefectoId === this.mecanismoId()) &&
      (!texto || `${p.nombre} ${p.descripcion ?? ''}`.toLocaleLowerCase('es-CL').includes(texto))
    );
  });
  actualizarBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }
  seleccionarCategoria(categoria: string): void {
    this.categoria.set(categoria);
  }

  seleccionarPasoLuz(event: Event): void {
    this.pasoLuz.set((event.target as HTMLSelectElement).value);
  }
  seleccionarTela(event: Event): void {
    this.telaId.set(Number((event.target as HTMLSelectElement).value));
  }
  seleccionarMecanismo(event: Event): void {
    this.mecanismoId.set(Number((event.target as HTMLSelectElement).value));
  }
  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoria.set('TODAS');
    this.telaId.set(0);
    this.pasoLuz.set('TODOS');
    this.mecanismoId.set(0);
  }
  ionViewWillEnter(): void {
    forkJoin({
      productos: this.productoService.listarActivos(),
      telas: this.catalogoService.listarTelas(),
      mecanismos: this.catalogoService.listarMecanismos(),
    }).subscribe({
      next: ({ productos, telas, mecanismos }) => {
        this.productos.set(productos);
        this.telas.set(telas);
        this.mecanismos.set(mecanismos);
        this.cargando.set(false);
      },
      error: () => {
        this.errorCarga.set(true);
        this.cargando.set(false);
      },
    });
  }
}
