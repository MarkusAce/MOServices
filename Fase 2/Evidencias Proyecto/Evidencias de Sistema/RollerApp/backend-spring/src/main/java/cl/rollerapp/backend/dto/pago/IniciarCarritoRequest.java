package cl.rollerapp.backend.dto.pago;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record IniciarCarritoRequest(@NotEmpty @Size(max = 20) List<@Valid Item> items) {
    public record Item(@NotNull @Positive Long cotizacionId, @Positive Long bloqueId) {}
}
