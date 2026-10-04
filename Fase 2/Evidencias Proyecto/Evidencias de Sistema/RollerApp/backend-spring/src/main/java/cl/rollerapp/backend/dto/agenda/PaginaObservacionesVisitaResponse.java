package cl.rollerapp.backend.dto.agenda;

import org.springframework.data.domain.Page;
import java.util.List;


public record PaginaObservacionesVisitaResponse(List<ObservacionVisitaResponse> content, int number,
        boolean last, int totalPages, long totalElements) {
    public static PaginaObservacionesVisitaResponse desde(Page<ObservacionVisitaResponse> p) {
        return new PaginaObservacionesVisitaResponse(p.getContent(), p.getNumber(), p.isLast(), p.getTotalPages(), p.getTotalElements());
    }
}
