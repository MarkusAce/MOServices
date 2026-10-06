package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.model.PasoLuz;
import jakarta.validation.constraints.NotNull;

public record ActualizarPasoLuzRequest(@NotNull PasoLuz pasoLuz) { }
