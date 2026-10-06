package cl.rollerapp.backend.dto.pago;

import jakarta.validation.constraints.NotNull;

public record IniciarPagoRequest(@NotNull(message = "Falta indicar la cotización a pagar.") Long cotizacionId) {
}
