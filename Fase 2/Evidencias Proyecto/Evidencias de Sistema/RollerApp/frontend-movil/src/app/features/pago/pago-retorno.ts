import { Subscription, switchMap, map, catchError, of } from 'rxjs';
import { CarritoService } from '../../core/services/carrito.service';
import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonSpinner, IonText,
} from '@ionic/angular';
import { PagoService } from '../../core/services/pago.service';
import { ConfirmarPagoResponse } from '../../core/models/pago.model';

@Component({
  selector: 'app-pago-retorno',
  imports: [CurrencyPipe, IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonSpinner, IonText],
  templateUrl: './pago-retorno.html',
})
export class PagoRetornoComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private pagoService = inject(PagoService);
  private carrito = inject(CarritoService);

  procesando = signal(true);
  resultado = signal<ConfirmarPagoResponse | null>(null);
  error = signal<string | null>(null);

  avisoCarrito = signal('');
  private token: string | null = null;
  private parametros?: Subscription;
  private confirmacion?: Subscription;

  ngOnInit(): void {
    this.parametros = this.route.queryParamMap.subscribe(parametros => {
      this.token = parametros.get('token');
      this.confirmar();
    });
  }

  confirmar(): void {
    this.confirmacion?.unsubscribe();
    this.resultado.set(null);
    this.error.set(null);
    this.avisoCarrito.set('');
    this.procesando.set(true);
    if (!this.token) {
      this.error.set('No se recibió un token de pago válido.');
      this.procesando.set(false);
      return;
    }
    this.confirmacion = this.pagoService.confirmar(this.token).pipe(
      switchMap(respuesta => this.carrito.sincronizar().pipe(
        map(() => respuesta),
        catchError(() => {
          this.avisoCarrito.set('El resultado del pago está confirmado. No se pudo actualizar el carrito; actualízalo antes de otra compra.');
          return of(respuesta);
        }),
      )),
    ).subscribe({
      next: respuesta => { this.resultado.set(respuesta); this.procesando.set(false); },
      error: err => {
        this.error.set(err.error?.mensaje ?? err.error?.error ?? 'No se pudo consultar el resultado. Revisa Mis pedidos antes de intentar otro pago.');
        this.procesando.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    this.parametros?.unsubscribe();
    this.confirmacion?.unsubscribe();
  }

  reintentarPago(): void {
    const ids = this.resultado()?.cotizacionIds ?? [];
    if (ids.length === 1) {
      void this.router.navigate(['/agenda'], { queryParams: { cotizacionId: ids[0] } });
      return;
    }
    if (ids.length > 1 && ids.every(id => this.carrito.contiene(id))) {
      void this.router.navigateByUrl('/tabs/carrito');
      return;
    }
    void this.router.navigateByUrl('/tabs/mis-pedidos');
  }

  irAMisPedidos(): void {
    this.router.navigateByUrl('/tabs/mis-pedidos');
  }

  irAlCatalogo(): void {
    this.router.navigateByUrl('/tabs/catalogo');
  }
}
