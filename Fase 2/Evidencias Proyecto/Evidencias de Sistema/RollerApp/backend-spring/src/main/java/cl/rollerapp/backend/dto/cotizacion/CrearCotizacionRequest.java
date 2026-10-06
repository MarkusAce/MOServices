package cl.rollerapp.backend.dto.cotizacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CrearCotizacionRequest(
        @NotNull(message = "Selecciona un producto.") Long productoId,
        @NotNull(message = "Selecciona una tela.") Long telaId,
        @NotNull(message = "Selecciona un mecanismo.") Long mecanismoId,
        @NotNull @DecimalMin(value = "1", message = "El ancho debe ser mayor a 0.") BigDecimal anchoCm,
        @NotNull @DecimalMin(value = "1", message = "El alto debe ser mayor a 0.") BigDecimal altoCm,
        @jakarta.validation.constraints.NotBlank(message = "Ingresa la dirección de instalación.") String direccion,
        @jakarta.validation.constraints.NotBlank(message = "Ingresa la comuna.") String comuna,
        List<Long> servicioIds,
        String placeId,
        boolean direccionManual,
        @jakarta.validation.constraints.Size(max=500) String referenciasDireccion
) {
    public CrearCotizacionRequest(Long productoId, Long telaId, Long mecanismoId,
            BigDecimal anchoCm, BigDecimal altoCm, String direccion, String comuna,
            List<Long> servicioIds, String placeId) {
        this(productoId,telaId,mecanismoId,anchoCm,altoCm,direccion,comuna,servicioIds,placeId,false,null);
    }
    public CrearCotizacionRequest(Long productoId, Long telaId, Long mecanismoId,
            BigDecimal anchoCm, BigDecimal altoCm, String direccion, String comuna,
            List<Long> servicioIds) {
        this(productoId, telaId, mecanismoId, anchoCm, altoCm, direccion, comuna, servicioIds, null);
    }
}
