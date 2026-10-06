package cl.rollerapp.backend.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record IncidenciaPagoAdminResponse(
        Long pagoId, Long cotizacionId, Long pedidoId, String ordenCompra,
        BigDecimal monto, String estado, String motivo, LocalDateTime creadoEn) {}
