package cl.rollerapp.backend.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearResenaRequest(
        @NotBlank(message = "Completa la descripción.") String descripcion,
        @NotNull @Min(value = 1, message = "La calidad debe estar entre 1 y 5.") @Max(5) Integer calidad,
        @NotNull @Min(value = 1, message = "El servicio debe estar entre 1 y 5.") @Max(5) Integer servicio
) {
}
