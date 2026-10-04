package cl.rollerapp.backend.dto.agenda;

import cl.rollerapp.backend.model.AgendaBloque;
import cl.rollerapp.backend.model.enums.EstadoAgenda;
import java.time.LocalDate;
import java.time.LocalTime;

public record AgendaAdminResponse(Long id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                  EstadoAgenda estado, Long tecnicoId, java.time.LocalDateTime atencionInicio, java.time.LocalDateTime atencionFin) {
    public static AgendaAdminResponse desde(AgendaBloque b) {
        return new AgendaAdminResponse(b.getId(), b.getFecha(), b.getHoraInicio(), b.getHoraFin(), b.getEstado(),
                b.getTecnico() != null ? b.getTecnico().getId() : null, b.getAtencionInicio(), b.getAtencionFin());
    }
}
