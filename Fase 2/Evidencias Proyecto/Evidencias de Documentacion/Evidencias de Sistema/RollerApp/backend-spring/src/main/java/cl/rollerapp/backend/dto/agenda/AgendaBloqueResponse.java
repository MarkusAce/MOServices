package cl.rollerapp.backend.dto.agenda;

import cl.rollerapp.backend.model.AgendaBloque;
import cl.rollerapp.backend.model.enums.EstadoAgenda;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendaBloqueResponse(Long id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, EstadoAgenda estado) {
    public static AgendaBloqueResponse desde(AgendaBloque b) {
        return new AgendaBloqueResponse(b.getId(), b.getFecha(), b.getHoraInicio(), b.getHoraFin(), b.getEstado());
    }
}
