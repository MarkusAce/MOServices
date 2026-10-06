package cl.rollerapp.backend.dto.producto;
import jakarta.validation.constraints.NotNull;
public record DisponibilidadServicioRequest(@NotNull Boolean activo) {}
