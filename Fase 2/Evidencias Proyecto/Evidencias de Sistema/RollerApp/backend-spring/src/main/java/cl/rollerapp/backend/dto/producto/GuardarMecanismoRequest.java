package cl.rollerapp.backend.dto.producto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record GuardarMecanismoRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion, @NotNull @DecimalMin("0.0") @DecimalMax("99999999.99") BigDecimal valorFijo, @NotNull Boolean activo) {}
