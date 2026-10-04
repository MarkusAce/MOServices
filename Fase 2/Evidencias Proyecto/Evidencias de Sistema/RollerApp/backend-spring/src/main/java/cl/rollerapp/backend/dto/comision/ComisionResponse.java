package cl.rollerapp.backend.dto.comision;

import cl.rollerapp.backend.model.Comision;
import cl.rollerapp.backend.model.enums.EstadoComision;
import cl.rollerapp.backend.model.enums.Rol;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComisionResponse(Long id, Long pedidoId, Long usuarioId, String usuarioNombre,
                               Rol rol, BigDecimal porcentaje, BigDecimal monto,
                               EstadoComision estado, LocalDateTime creadaEn, LocalDateTime pagadaEn) {
    public static ComisionResponse desde(Comision c) {
        return new ComisionResponse(c.getId(), c.getPedido().getId(), c.getUsuario().getId(),
            c.getUsuario().getNombre() + " " + c.getUsuario().getApellido(), c.getUsuario().getRol(),
            c.getPorcentaje(), c.getMonto(), c.getEstado(), c.getCreadaEn(), c.getPagadaEn());
    }
}
