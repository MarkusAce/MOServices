import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MesKpi { mes: string; ventas: number; compras: number; usuarios: number; cotizaciones: number; concretadas: number; }
export interface AdminKpis {
  tiempoPromedioAtencionMinutos: number | null;
  visitasConDuracion: number;
  cotizacionesConcretadasMes: number;
  conversionPorcentaje: number;
  visitasAgendadas: number;
  visitasCompletadas: number;
  visitasCanceladas: number;
  ventasMes: number;
  comprasMes: number;
  usuariosMes: number;
  enConfeccion: number;
  enTerreno: number;
  cotizacionesMes: number;
  cotizacionesPendientesMes: number;
  actualizadoEn: string;
  meses: MesKpi[];
}

@Injectable({ providedIn: 'root' })
export class AdminKpiService {
  private http = inject(HttpClient);
  obtener(): Observable<AdminKpis> {
    return this.http.get<AdminKpis>(`${environment.apiUrl}/admin/kpis`);
  }
}
