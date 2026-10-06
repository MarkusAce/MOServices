import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();

  const base = new URL(environment.apiUrl, window.location.origin);
  const destino = new URL(req.url, window.location.origin);
  if (!token || destino.origin !== base.origin || !(destino.pathname === base.pathname || destino.pathname.startsWith(base.pathname + '/'))) return next(req);

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
