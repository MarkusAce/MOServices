import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EMPTY, Subscription, catchError, interval, startWith, switchMap, finalize } from 'rxjs';
import { IonContent, IonHeader, IonTitle, IonToolbar, IonSpinner, IonRefresher, IonRefresherContent } from '@ionic/angular';
import { AuthService } from '../../../core/services/auth.service';
import { AdminKpiService, AdminKpis } from '../../../core/services/admin-kpi.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink, IonContent, IonHeader, IonTitle, IonToolbar, IonSpinner, IonRefresher, IonRefresherContent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboardComponent implements OnDestroy {
  auth = inject(AuthService);
  private kpiService = inject(AdminKpiService);
  private consulta?: Subscription;
  kpis = signal<AdminKpis | null>(null);
  error = signal(false);
  cargando = signal(true);

  ionViewWillEnter(): void {
    this.consulta?.unsubscribe();
    this.consulta = interval(30_000).pipe(
      startWith(0),
      switchMap(() => this.kpiService.obtener().pipe(catchError(() => {
        this.error.set(true);
        this.cargando.set(false);
        return EMPTY;
      }))),
    ).subscribe(datos => {
      this.kpis.set(datos);
      this.error.set(false);
      this.cargando.set(false);
    });
  }
  actualizar(event: CustomEvent): void {
    this.kpiService.obtener().pipe(finalize(() => (event.target as HTMLIonRefresherElement).complete())).subscribe({
      next: datos => { this.kpis.set(datos); this.error.set(false); this.cargando.set(false); },
      error: () => { this.error.set(true); this.cargando.set(false); },
    });
  }
  ionViewWillLeave(): void { this.consulta?.unsubscribe(); }
  ngOnDestroy(): void { this.consulta?.unsubscribe(); }
}
