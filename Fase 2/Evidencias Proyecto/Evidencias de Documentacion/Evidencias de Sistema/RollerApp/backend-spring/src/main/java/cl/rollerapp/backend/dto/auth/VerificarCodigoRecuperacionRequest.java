package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerificarCodigoRecuperacionRequest(
        @NotBlank String correo,
        @NotBlank String codigo
) {
}
