package cl.rollerapp.backend.dto.pago;

public record ConfirmarPagoResponse(
        boolean aprobado,
        String estadoTransaccion,
        String ordenCompra,
        java.math.BigDecimal monto,
        String codigoAutorizacion,
        String tarjetaUltimosDigitos,
        Long pedidoId,
        java.util.List<Long> pedidoIds,
        java.util.List<Long> cotizacionIds
) { }
