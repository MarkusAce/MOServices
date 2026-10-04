package cl.rollerapp.backend.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ActualizarComisionTecnicoRequest(
        @NotNull @DecimalMin("0.00") BigDecimal monto) { }
