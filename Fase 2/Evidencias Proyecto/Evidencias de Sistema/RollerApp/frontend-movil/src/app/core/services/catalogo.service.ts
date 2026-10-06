import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Mecanismo, ServicioAdicional, Tela, PasoLuz } from '../models/producto.model';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  listarTelas(): Observable<Tela[]> {
    return this.http.get<Tela[]>(`${this.baseUrl}/telas`);
  }

  actualizarPrecioTela(id: number, precioM2: number): Observable<Tela> {
    return this.http.put<Tela>(`${this.baseUrl}/telas/${id}/precio`, { precioM2 });
  }

  actualizarPasoLuz(id: number, pasoLuz: PasoLuz): Observable<Tela> {
    return this.http.put<Tela>(`${this.baseUrl}/telas/${id}/paso-luz`, { pasoLuz });
  }

  listarTodasTelas(): Observable<Tela[]> { return this.http.get<Tela[]>(`${this.baseUrl}/telas/admin/todas`); }
  guardarTela(id: number | null, datos: Omit<Tela, 'id'>): Observable<Tela> {
    return id === null ? this.http.post<Tela>(`${this.baseUrl}/telas`, datos) : this.http.put<Tela>(`${this.baseUrl}/telas/${id}`, datos);
  }
  archivarTela(id: number): Observable<Tela> { return this.http.delete<Tela>(`${this.baseUrl}/telas/${id}`); }

  listarTodosMecanismos(): Observable<Mecanismo[]> { return this.http.get<Mecanismo[]>(`${this.baseUrl}/mecanismos/admin/todos`); }

  guardarMecanismo(id: number | null, datos: Omit<Mecanismo, 'id'>): Observable<Mecanismo> {
    return id === null ? this.http.post<Mecanismo>(`${this.baseUrl}/mecanismos`, datos) : this.http.put<Mecanismo>(`${this.baseUrl}/mecanismos/${id}`, datos);
  }
  archivarMecanismo(id: number): Observable<Mecanismo> { return this.http.delete<Mecanismo>(`${this.baseUrl}/mecanismos/${id}`); }

  listarMecanismos(): Observable<Mecanismo[]> {
    return this.http.get<Mecanismo[]>(`${this.baseUrl}/mecanismos`);
  }

  actualizarComisionTecnico(id: number, monto: number): Observable<ServicioAdicional> {
    return this.http.put<ServicioAdicional>(`${this.baseUrl}/servicios/${id}/comision-tecnico`, { monto });
  }

  listarServicios(): Observable<ServicioAdicional[]> {
    return this.http.get<ServicioAdicional[]>(`${this.baseUrl}/servicios`);
  }
}
