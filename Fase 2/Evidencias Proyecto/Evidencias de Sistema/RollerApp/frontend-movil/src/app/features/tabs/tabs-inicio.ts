import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({ selector: 'app-tabs-inicio', template: '' })
export class TabsInicioComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  constructor() {
    const destino = this.auth.esAdmin() ? '/tabs/admin' :
      this.auth.esTecnico() ? '/tabs/mis-visitas' : this.auth.esVendedor() ? '/tabs/admin-pedidos' : '/tabs/catalogo';
    queueMicrotask(() => this.router.navigateByUrl(destino, { replaceUrl: true }));
  }
}
