package cl.rollerapp.backend.dto.producto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record GuardarServicioRequest(
 @NotBlank @Size(max=100) String nombre,
 @Size(max=255) String descripcion,
 @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) BigDecimal precio,
 @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) BigDecimal comisionTecnico,
 @NotNull Boolean activo) {}
