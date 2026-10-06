package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerContrasenaRequest(
        @NotBlank(message = "Falta el token de recuperación.") String resetToken,
        @NotBlank @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres.") String nuevaContrasena
) {
}
