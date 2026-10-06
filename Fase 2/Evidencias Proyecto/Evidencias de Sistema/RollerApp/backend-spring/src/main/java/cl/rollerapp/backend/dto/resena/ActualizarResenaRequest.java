package cl.rollerapp.backend.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ActualizarResenaRequest(
        String descripcion,
        @Min(1) @Max(5) Integer calidad,
        @Min(1) @Max(5) Integer servicio
) {
}
