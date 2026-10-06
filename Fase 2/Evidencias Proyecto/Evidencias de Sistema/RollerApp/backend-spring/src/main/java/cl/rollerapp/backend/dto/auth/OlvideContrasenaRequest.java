package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OlvideContrasenaRequest(
        @NotBlank @Email(message = "Ingresa un correo válido.") String correo
) {
}
