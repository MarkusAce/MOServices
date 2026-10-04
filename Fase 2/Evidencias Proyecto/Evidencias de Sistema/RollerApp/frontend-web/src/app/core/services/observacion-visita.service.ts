import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ObservacionVisita {
  id: number; visitaId: number; autorId: number; autorNombre: string;
  autorRol: string; texto: string; creadoEn: string;
}
export interface PaginaObservaciones { content: ObservacionVisita[]; number: number; last: boolean; }

@Injectable({ providedIn: 'root' })
export class ObservacionVisitaService {
  private http = inject(HttpClient);
  listar(id: number, pagina = 0): Observable<PaginaObservaciones> {
    return this.http.get<PaginaObservaciones>(`${environment.apiUrl}/agenda/bloques/${id}/observaciones`, { params: { pagina } });
  }
  agregar(id: number, texto: string): Observable<ObservacionVisita> {
    return this.http.post<ObservacionVisita>(`${environment.apiUrl}/agenda/bloques/${id}/observaciones`, { texto });
  }
}
