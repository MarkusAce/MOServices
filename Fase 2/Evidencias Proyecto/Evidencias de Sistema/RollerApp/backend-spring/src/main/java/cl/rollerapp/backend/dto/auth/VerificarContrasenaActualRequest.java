package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerificarContrasenaActualRequest(
        @NotBlank(message = "Ingresa tu contraseña actual.") String contrasenaActual
) {
}
