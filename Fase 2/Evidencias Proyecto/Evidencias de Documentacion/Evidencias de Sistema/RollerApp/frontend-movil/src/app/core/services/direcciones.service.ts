import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface SugerenciaDireccion { placeId: string; texto: string; }
export interface DireccionVerificada {
  placeId: string; direccion: string; comuna: string; latitud: number; longitud: number;
}
@Injectable({ providedIn: 'root' })
export class DireccionesService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/direcciones`;
  configuracion() { return this.http.get<{habilitado: boolean}>(`${this.url}/configuracion`); }
  sugerencias(texto: string, token: string) {
    return this.http.get<SugerenciaDireccion[]>(`${this.url}/sugerencias`,
      { params: new HttpParams().set('texto', texto).set('token', token) });
  }
  detalle(placeId: string, token: string) {
    return this.http.get<DireccionVerificada>(`${this.url}/detalle/${encodeURIComponent(placeId)}`,
      { params: new HttpParams().set('token', token) });
  }
}
