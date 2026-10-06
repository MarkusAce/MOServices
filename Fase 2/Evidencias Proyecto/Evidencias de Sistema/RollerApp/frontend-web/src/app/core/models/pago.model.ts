export interface IniciarPagoResponse {
  url: string;
  token: string;
}

export interface ConfirmarPagoResponse {
  aprobado: boolean;
  estadoTransaccion: string;
  ordenCompra: string;
  monto: number;
  codigoAutorizacion: string | null;
  tarjetaUltimosDigitos: string | null;
  pedidoId: number | null;
  pedidoIds?: number[];
  cotizacionIds?: number[];
}
