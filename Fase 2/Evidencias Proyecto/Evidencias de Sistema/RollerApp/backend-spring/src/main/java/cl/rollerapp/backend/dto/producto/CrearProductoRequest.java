package cl.rollerapp.backend.dto.producto;

import cl.rollerapp.backend.model.enums.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearProductoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank String descripcion,
        @NotNull Categoria categoria,
        @Size(max = 255) String imagenPrincipal,
        Long telaDefectoId,
        Long mecanismoDefectoId,
        @DecimalMin("1") @DecimalMax("9999.99") BigDecimal anchoMaxCm,
        @DecimalMin("1") @DecimalMax("9999.99") BigDecimal altoMaxCm
) {
}
