package cl.rollerapp.backend.dto.auth;
import cl.rollerapp.backend.model.enums.Rol;
import jakarta.validation.constraints.*;
public record CrearPersonalRequest(
 @NotBlank @Size(max=100) String nombre, @NotBlank @Size(max=100) String apellido,
 @NotBlank @Email @Size(max=150) String correo,
 @NotBlank @Size(min=8,max=64) String contrasena, @NotNull Rol rol) {}
