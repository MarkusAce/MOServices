package cl.rollerapp.backend.dto.producto;

import cl.rollerapp.backend.model.Producto;
import cl.rollerapp.backend.model.enums.Categoria;

import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        Categoria categoria,
        String imagenPrincipal,
        List<String> imagenes,
        Long telaDefectoId,
        Long mecanismoDefectoId,
        BigDecimal anchoMaxCm,
        BigDecimal altoMaxCm,
        Boolean activo
) {
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCategoria(),
                p.getImagenPrincipal(),
                p.getImagenes().stream().map(img -> img.getUrl()).toList(),
                p.getTelaDefecto() != null ? p.getTelaDefecto().getId() : null,
                p.getMecanismoDefecto() != null ? p.getMecanismoDefecto().getId() : null,
                p.getAnchoMaxCm(),
                p.getAltoMaxCm(),
                p.getActivo()
        );
    }
}
