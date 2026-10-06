package cl.rollerapp.backend.dto.cotizacion;

import cl.rollerapp.backend.model.Cotizacion;
import cl.rollerapp.backend.model.enums.EstadoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CotizacionResponse(
        Long id,
        Long productoId,
        String productoNombre,
        Long telaId,
        String telaNombre,
        Long mecanismoId,
        String mecanismoNombre,
        BigDecimal anchoCm,
        BigDecimal altoCm,
        BigDecimal metrosCuadrados,
        BigDecimal metrosCuadradosFacturados,
        boolean aplicaCobroMinimo,
        String mensajeCobroMinimo,
        BigDecimal valorTela,
        BigDecimal valorMecanismo,
        BigDecimal valorServicios,
        BigDecimal total,
        String direccion,
        String comuna,
        EstadoCotizacion estado,
        LocalDateTime creadoEn,
        LocalDateTime expiraEn,
        boolean requiereVisita,
        java.util.List<ServicioCotizado> servicios
) {
    public record ServicioCotizado(Long id, String nombre, BigDecimal precio) {}

    private static boolean aplicaMinimo(Cotizacion c) {
        return c.getMetrosCuadrados() != null && c.getMetrosCuadrados().compareTo(BigDecimal.ONE) < 0;
    }

    private static BigDecimal areaFacturada(Cotizacion c) {
        return c.getMetrosCuadrados() == null ? null : c.getMetrosCuadrados().max(BigDecimal.ONE);
    }

    public static CotizacionResponse desde(Cotizacion c) {
        return new CotizacionResponse(
                c.getId(),
                c.getProducto().getId(),
                c.getProducto().getNombre(),
                c.getTela().getId(),
                c.getTela().getNombre(),
                c.getMecanismo().getId(),
                c.getMecanismo().getNombre(),
                c.getAnchoCm(),
                c.getAltoCm(),
                c.getMetrosCuadrados(),
                areaFacturada(c),
                aplicaMinimo(c),
                aplicaMinimo(c) ? "Se factura un mínimo de 1,00 m² de tela por cortina." : null,
                c.getValorTela(),
                c.getValorMecanismo(),
                c.getValorServicios(),
                c.getTotal(),
                c.getDireccion(),
                c.getComuna(),
                c.getEstado(),
                c.getCreadoEn(),
                c.getCreadoEn() == null ? null : c.getCreadoEn().plusDays(15),
                c.requiereVisita(),
                c.getServicios() == null ? java.util.List.of() : c.getServicios().stream()
                        .filter(s -> s.getServicio() != null)
                        .map(s -> new ServicioCotizado(s.getServicio().getId(), s.getServicio().getNombre(), s.getPrecioAplicado())).toList()
        );
    }
}
