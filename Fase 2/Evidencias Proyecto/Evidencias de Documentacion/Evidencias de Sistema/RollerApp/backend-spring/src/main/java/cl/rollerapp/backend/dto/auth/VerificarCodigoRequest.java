package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerificarCodigoRequest(@NotBlank(message = "Ingresa el código.") String codigo) {
}
