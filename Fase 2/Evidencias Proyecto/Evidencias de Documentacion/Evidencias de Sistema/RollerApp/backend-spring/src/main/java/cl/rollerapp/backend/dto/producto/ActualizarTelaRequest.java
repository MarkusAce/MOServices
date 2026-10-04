package cl.rollerapp.backend.dto.producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record ActualizarTelaRequest(
        @NotNull @DecimalMin(value = "0", message = "El precio no puede ser negativo.") BigDecimal precioM2
) {
}
