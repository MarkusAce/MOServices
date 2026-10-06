import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConfirmarPagoResponse, IniciarPagoResponse } from '../models/pago.model';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/pagos`;

  iniciar(cotizacionId: number): Observable<IniciarPagoResponse> {
    return this.http.post<IniciarPagoResponse>(`${this.baseUrl}/iniciar`, { cotizacionId });
  }

  iniciarCarrito(items: {cotizacionId:number; bloqueId:number | null}[]): Observable<IniciarPagoResponse> {
    return this.http.post<IniciarPagoResponse>(`${this.baseUrl}/carrito/iniciar`, { items });
  }

  confirmar(tokenWebpay: string): Observable<ConfirmarPagoResponse> {
    return this.http.post<ConfirmarPagoResponse>(`${this.baseUrl}/confirmar`, { tokenWebpay });
  }

  async redirigirAWebpay(url: string, token: string): Promise<void> {
    const destino = new URL(url);
    if (destino.protocol !== 'https:' || !['webpay3g.transbank.cl', 'webpay3gint.transbank.cl'].includes(destino.hostname) || destino.username || destino.password || (destino.port && destino.port !== '443') || !token.trim() || token.length > 200) {
      throw new Error('Destino de pago inválido.');
    }
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = url;

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'token_ws';
    input.value = token;

    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
  }
}
