package cl.rollerapp.backend.dto.pago;

import jakarta.validation.constraints.NotNull;

public record ReagendarPedidoRequest(@NotNull Long bloqueId) {}
