package cl.rollerapp.backend.dto.agenda;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CrearBloqueRequest(@NotNull LocalDate fecha, @NotNull LocalTime horaInicio,
                                 @NotNull LocalTime horaFin, @NotNull Long tecnicoId) {}
