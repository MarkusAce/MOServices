import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Resena } from '../models/resena.model';

@Injectable({ providedIn: 'root' })
export class ResenaService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/resenas`;

  listarTodas(): Observable<Resena[]> {
    return this.http.get<Resena[]>(this.baseUrl);
  }

  crear(datos: { descripcion: string; calidad: number; servicio: number }): Observable<Resena> {
    return this.http.post<Resena>(this.baseUrl, datos);
  }

  actualizar(id: number, datos: Partial<{ descripcion: string; calidad: number; servicio: number }>): Observable<Resena> {
    return this.http.put<Resena>(`${this.baseUrl}/${id}`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
