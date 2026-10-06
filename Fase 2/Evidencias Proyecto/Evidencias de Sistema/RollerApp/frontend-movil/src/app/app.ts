import { Browser } from '@capacitor/browser';
import { App as CapacitorApp } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { Router } from '@angular/router';
import { NgZone, inject } from '@angular/core';
import { Component } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular';

@Component({
  selector: 'app-root',
  imports: [IonApp, IonRouterOutlet],
  template: `
    <ion-app>
      <ion-router-outlet></ion-router-outlet>
    </ion-app>
  `,
})
export class App {
  private router = inject(Router);
  private zone = inject(NgZone);
  constructor() {
    if (!Capacitor.isNativePlatform()) return;
    const abrir = (url: string) => {
      let destino: URL;
      try { destino = new URL(url); } catch { return; }
      if (destino.protocol !== 'rollerapp:' || destino.hostname !== 'pago' || destino.pathname !== '/retorno') return;
      void Browser.close().catch(() => {});
      this.zone.run(() => void this.router.navigateByUrl('/pago/retorno' + destino.search));
    };
    void CapacitorApp.addListener('appUrlOpen', evento => abrir(evento.url)).catch(() => {});
    void CapacitorApp.getLaunchUrl().then(resultado => { if (resultado?.url) abrir(resultado.url); }).catch(() => {});
  }
}
