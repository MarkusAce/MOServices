import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from '../models/producto.model';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/productos`;

  listarActivos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.baseUrl);
  }

  obtenerPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/${id}`);
  }

  listarTodos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/admin/todos`);
  }

  subirImagen(archivo: File): Observable<{ imagenPrincipal: string }> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<{ imagenPrincipal: string }>(`${this.baseUrl}/imagenes`, datos);
  }

  crear(datos: Partial<Producto>): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, datos);
  }

  editar(id: number, datos: Partial<Producto>): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, datos);
  }

  archivar(id: number): Observable<Producto> {
    return this.http.delete<Producto>(`${this.baseUrl}/${id}`);
  }

  actualizarEstado(id: number, activo: boolean): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}/estado`, { activo });
  }
}
