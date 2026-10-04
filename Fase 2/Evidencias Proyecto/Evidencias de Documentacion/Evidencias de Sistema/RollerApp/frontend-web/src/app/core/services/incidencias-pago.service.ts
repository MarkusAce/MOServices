import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface IncidenciaPago {
  pagoId: number;
  cotizacionId: number;
  pedidoId: number | null;
  ordenCompra: string;
  monto: number;
  estado: string;
  motivo: 'PAGO_SIN_PEDIDO' | 'PEDIDO_SIN_VISITA' | 'PAGO_PENDIENTE_CONCILIACION';
  creadoEn: string;
}

@Injectable({ providedIn: 'root' })
export class IncidenciasPagoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/pagos/admin/incidencias`;
  listar() { return this.http.get<IncidenciaPago[]>(this.url); }
  revisar(pagoId: number) {
    return this.http.post<{ mensaje: string }>(`${this.url}/${pagoId}/revisar`, {});
  }
  reagendar(pedidoId: number, bloqueId: number) {
    return this.http.put<void>(`${this.url}/pedidos/${pedidoId}/reagendar`, { bloqueId });
  }
}
