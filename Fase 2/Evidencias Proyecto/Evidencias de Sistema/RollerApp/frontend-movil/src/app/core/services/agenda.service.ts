import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AgendaBloque } from '../models/agenda.model';

export interface AgendaAdminBloque extends AgendaBloque { atencionInicio: string | null; atencionFin: string | null; tecnicoId: number | null; }

@Injectable({ providedIn: 'root' })
export class AgendaService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/agenda`;

  listarDisponibles(): Observable<AgendaBloque[]> {
    return this.http.get<AgendaBloque[]>(`${this.baseUrl}/disponibles`);
  }

  preReservar(bloqueId: number, cotizacionId: number): Observable<AgendaBloque> {
    return this.http.put<AgendaBloque>(`${this.baseUrl}/bloques/${bloqueId}/pre-reservar`, { cotizacionId });
  }

  listarAdmin(): Observable<AgendaAdminBloque[]> {
    return this.http.get<AgendaAdminBloque[]>(`${this.baseUrl}/admin/bloques`);
  }

  crear(datos: { fecha: string; horaInicio: string; horaFin: string; tecnicoId: number }): Observable<AgendaBloque> {
    return this.http.post<AgendaBloque>(`${this.baseUrl}/admin/bloques`, datos);
  }

  cancelar(id: number): Observable<AgendaBloque> {
    return this.http.put<AgendaBloque>(`${this.baseUrl}/admin/bloques/${id}/cancelar`, {});
  }

  iniciar(id: number): Observable<AgendaBloque> { return this.http.put<AgendaBloque>(`${this.baseUrl}/bloques/${id}/iniciar`, {}); }

  completar(id: number): Observable<AgendaBloque> {
    return this.http.put<AgendaBloque>(`${this.baseUrl}/bloques/${id}/completar`, {});
  }

  asignarTecnico(id: number, tecnicoId: number): Observable<AgendaBloque> {
    return this.http.put<AgendaBloque>(`${this.baseUrl}/admin/bloques/${id}/tecnico`, { tecnicoId });
  }
}
