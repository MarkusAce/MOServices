import { environment } from '../../../environments/environment';

export function imagenProducto(ruta: string | null): string {
  if (!ruta) return '';
  if (/^https?:\/\//i.test(ruta)) return ruta;
  if (ruta.startsWith('/api/productos/imagenes/')) {
    return environment.apiUrl.replace(/\/api\/?$/, '') + ruta;
  }
  const limpia = ruta.replace(/^\/?assets\/img\//i, '').replace(/^\//, '');
  return 'assets/img/' + limpia;
}
