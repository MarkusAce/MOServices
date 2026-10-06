package cl.rollerapp.backend.dto.auth;
import cl.rollerapp.backend.model.enums.Rol;
import jakarta.validation.constraints.NotNull;
public record CambiarRolRequest(@NotNull Rol rol) {}
