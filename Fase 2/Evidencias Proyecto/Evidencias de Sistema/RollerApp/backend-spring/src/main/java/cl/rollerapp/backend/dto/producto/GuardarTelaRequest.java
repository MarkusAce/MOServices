package cl.rollerapp.backend.dto.producto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import cl.rollerapp.backend.model.PasoLuz;

public record GuardarTelaRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion, @NotNull PasoLuz pasoLuz, @NotNull @DecimalMin("0.0") @DecimalMax("99999999.99") BigDecimal precioM2, @NotNull Boolean activo) {}
