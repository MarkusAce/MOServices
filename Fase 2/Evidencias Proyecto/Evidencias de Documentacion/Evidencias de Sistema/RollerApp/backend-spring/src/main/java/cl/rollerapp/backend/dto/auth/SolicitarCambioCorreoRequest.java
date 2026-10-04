package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarCambioCorreoRequest(@NotBlank String contrasenaActual,
                                          @NotBlank @Email String nuevoCorreo) {}
