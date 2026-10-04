package cl.rollerapp.backend.dto.postventa;

import cl.rollerapp.backend.model.enums.EstadoPostventa;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPostventaRequest(@NotNull EstadoPostventa estado) {
}
