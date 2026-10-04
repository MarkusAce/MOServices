package cl.rollerapp.backend.dto.agenda;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearObservacionVisitaRequest(
        @NotBlank(message = "Escribe una observación.")
        @Size(max = 2000, message = "La observación admite hasta 2000 caracteres.") String texto) { }
