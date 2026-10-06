import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { CatalogoService } from '../../core/services/catalogo.service';
import { ServicioAdicional } from '../../core/models/producto.model';

@Component({
  standalone: true,
  imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, CurrencyPipe, RouterLink],
  templateUrl: './servicios.html',
  styleUrl: './servicios.scss',
})
export class ServiciosComponent  {
  private catalogo = inject(CatalogoService);
  servicios = signal<ServicioAdicional[]>([]);
  cargando = signal(false);
  error = signal(false);

  ionViewWillEnter(): void { this.cargar(); }

  cargar(): void {
    if (this.cargando()) return;
    this.cargando.set(true);
    this.error.set(false);
    this.catalogo.listarServicios().pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: servicios => this.servicios.set(servicios.filter(s => s.activo)),
      error: () => this.error.set(true),
    });
  }
}
