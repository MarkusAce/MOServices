package cl.rollerapp.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.") String nombre,
        @NotBlank(message = "El apellido es obligatorio.") String apellido,
        @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no tiene un formato válido.") String correo,
        @NotBlank(message = "La contraseña es obligatoria.") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String contrasena
) {
}
