import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comision, EstadoComision } from '../models/comision.model';

@Injectable({ providedIn: 'root' })
export class ComisionService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/comisiones`;

  listar(estado?: EstadoComision, usuarioId?: number): Observable<Comision[]> {
    let params = new HttpParams();
    if (estado) params = params.set('estado', estado);
    if (usuarioId != null) params = params.set('usuarioId', usuarioId);
    return this.http.get<Comision[]>(this.url, { params });
  }

  liquidar(id: number): Observable<Comision> {
    return this.http.patch<Comision>(`${this.url}/${id}/liquidar`, {});
  }
}
