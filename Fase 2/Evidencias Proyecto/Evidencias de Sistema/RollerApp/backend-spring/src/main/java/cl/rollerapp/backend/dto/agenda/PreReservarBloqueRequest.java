package cl.rollerapp.backend.dto.agenda;

import jakarta.validation.constraints.NotNull;

public record PreReservarBloqueRequest(@NotNull(message = "Falta indicar la cotización.") Long cotizacionId) {
}
