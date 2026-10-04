package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
        @NotBlank @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres.") String nuevaContrasena
) {
}
