package cl.rollerapp.backend.dto.postventa;

import cl.rollerapp.backend.model.PostventaSolicitud;
import cl.rollerapp.backend.model.enums.EstadoPostventa;

import java.time.LocalDateTime;

public record PostventaResponse(
        Long id, Long usuarioId, String nombre, String apellido, String correo,
        String telefono, String comuna, String descripcion, EstadoPostventa estado, LocalDateTime fecha
) {
    public static PostventaResponse desde(PostventaSolicitud s) {
        return new PostventaResponse(
                s.getId(),
                s.getUsuario() != null ? s.getUsuario().getId() : null,
                s.getNombre(), s.getApellido(), s.getCorreo(), s.getTelefono(),
                s.getComuna(), s.getDescripcion(), s.getEstado(), s.getFecha()
        );
    }
}
