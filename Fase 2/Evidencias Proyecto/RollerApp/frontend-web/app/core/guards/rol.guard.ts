import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export function rolGuard(rolesPermitidos: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.estaLogeado()) {
      router.navigateByUrl('/login');
      return false;
    }
    if (!rolesPermitidos.includes(auth.usuarioActual()?.rol ?? '')) {
      router.navigateByUrl('/');
      return false;
    }
    return true;
  };
}
