import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cotizacion, CrearCotizacionRequest } from '../models/cotizacion.model';

@Injectable({ providedIn: 'root' })
export class CotizacionService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/cotizaciones`;

  crear(datos: CrearCotizacionRequest): Observable<Cotizacion> {
    return this.http.post<Cotizacion>(this.baseUrl, datos);
  }

  renovar(id: number): Observable<Cotizacion> {
    return this.http.post<Cotizacion>(`${this.baseUrl}/${id}/renovar`, {});
  }

  listarPropias(): Observable<Cotizacion[]> {
    return this.http.get<Cotizacion[]>(this.baseUrl);
  }

  obtenerPorId(id: number): Observable<Cotizacion> {
    return this.http.get<Cotizacion>(`${this.baseUrl}/${id}`);
  }
}
