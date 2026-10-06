package cl.rollerapp.backend.dto.agenda;

import cl.rollerapp.backend.model.ObservacionVisita;
import java.time.Instant;

public record ObservacionVisitaResponse(Long id, Long visitaId, Long autorId,
        String autorNombre, String autorRol, String texto, Instant creadoEn) {
    public static ObservacionVisitaResponse desde(ObservacionVisita o) {
        return new ObservacionVisitaResponse(o.getId(), o.getBloque().getId(), o.getAutor().getId(),
                o.getAutor().getNombre() + " " + o.getAutor().getApellido(),
                o.getAutor().getRol().name(), o.getTexto(), o.getCreadoEn());
    }
}
