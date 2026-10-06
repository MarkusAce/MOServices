import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EstadoPostventa, PostventaSolicitud } from '../models/postventa.model';

@Injectable({ providedIn: 'root' })
export class PostventaService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/postventa`;

  crear(datos: {
    nombre: string; apellido: string; correo: string; telefono: string; comuna: string; descripcion: string;
  }): Observable<PostventaSolicitud> {
    return this.http.post<PostventaSolicitud>(this.baseUrl, datos);
  }

  listarTodas(): Observable<PostventaSolicitud[]> {
    return this.http.get<PostventaSolicitud[]>(`${this.baseUrl}/admin/todas`);
  }

  cambiarEstado(id: number, estado: EstadoPostventa): Observable<PostventaSolicitud> {
    return this.http.put<PostventaSolicitud>(`${this.baseUrl}/${id}/estado`, { estado });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
