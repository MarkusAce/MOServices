import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EstadoPedido, Pedido } from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/pedidos`;

  listarPropios(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.baseUrl);
  }

  listarNotificaciones(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.baseUrl}/notificaciones`);
  }

  marcarNotificacionesVistas(): Observable<{ actualizados: number }> {
    return this.http.put<{ actualizados: number }>(`${this.baseUrl}/notificaciones/marcar-vistos`, {});
  }

  listarTodos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.baseUrl}/admin/todos`);
  }

  listarMisVisitas(fecha: string): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.baseUrl}/mis-visitas`, { params: { fecha } });
  }

  asignarVendedor(id: number, vendedorId: number): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.baseUrl}/${id}/vendedor`, { vendedorId });
  }

  cambiarEstado(id: number, estado: EstadoPedido): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.baseUrl}/${id}/estado`, { estado });
  }
}
