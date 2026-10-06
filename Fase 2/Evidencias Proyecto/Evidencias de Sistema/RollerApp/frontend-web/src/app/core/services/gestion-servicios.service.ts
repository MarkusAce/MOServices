import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ServicioAdicional } from '../models/producto.model';
export type DatosServicio = Omit<ServicioAdicional, 'id'>;
@Injectable({ providedIn: 'root' })
export class GestionServiciosService {
 private http = inject(HttpClient);
 private url = `${environment.apiUrl}/admin/servicios`;
 listar() { return this.http.get<ServicioAdicional[]>(this.url); }
 guardar(id: number | null, datos: DatosServicio) {
  return id === null ? this.http.post<ServicioAdicional>(this.url, datos) : this.http.put<ServicioAdicional>(`${this.url}/${id}`, datos);
 }
 disponibilidad(id: number, activo: boolean) { return this.http.put<ServicioAdicional>(`${this.url}/${id}/disponibilidad`, { activo }); }
}
