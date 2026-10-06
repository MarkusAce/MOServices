package cl.rollerapp.backend.dto.agenda;

import jakarta.validation.constraints.NotNull;

public record AsignarTecnicoRequest(@NotNull Long tecnicoId) {}
