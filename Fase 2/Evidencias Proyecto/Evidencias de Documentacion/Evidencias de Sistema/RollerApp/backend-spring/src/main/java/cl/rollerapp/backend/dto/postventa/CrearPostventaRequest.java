package cl.rollerapp.backend.dto.postventa;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CrearPostventaRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email(message = "El correo no tiene un formato válido.") String correo,
        @NotBlank String telefono,
        @NotBlank String comuna,
        @NotBlank String descripcion
) {
}
