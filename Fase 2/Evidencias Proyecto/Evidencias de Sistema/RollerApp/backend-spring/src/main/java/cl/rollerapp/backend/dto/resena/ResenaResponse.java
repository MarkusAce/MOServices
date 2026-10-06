package cl.rollerapp.backend.dto.resena;

import cl.rollerapp.backend.model.Resena;

import java.time.LocalDateTime;

public record ResenaResponse(
        Long id, Long usuarioId, String nombre, String descripcion,
        Integer calidad, Integer servicio, LocalDateTime fecha,
        Boolean editada, LocalDateTime fechaEdicion
) {
    public static ResenaResponse desde(Resena r) {
        return new ResenaResponse(
                r.getId(),
                r.getUsuario() != null ? r.getUsuario().getId() : null,
                r.getNombre(), r.getDescripcion(), r.getCalidad(), r.getServicio(),
                r.getFecha(), r.getEditada(), r.getFechaEdicion()
        );
    }
}
