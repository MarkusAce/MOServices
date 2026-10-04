package cl.rollerapp.backend.dto.pedido;

import cl.rollerapp.backend.model.Pedido;
import cl.rollerapp.backend.model.enums.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import cl.rollerapp.backend.model.enums.EstadoAgenda;

public record PedidoResponse(
        Long id,
        Long cotizacionId,
        String productoNombre,
        BigDecimal total,
        String direccion,
        String comuna,
        EstadoPedido estado,
        Boolean visto,
        LocalDateTime creadoEn,
        Long vendedorId,
        String vendedorNombre,
        Long tecnicoId,
        String tecnicoNombre,
        Long visitaId,
        LocalDate visitaFecha,
        LocalTime visitaHoraInicio,
        LocalTime visitaHoraFin,
        EstadoAgenda visitaEstado,
        LocalDateTime atencionInicio,
        LocalDateTime atencionFin,
        boolean requiereVisita,
        Long clienteId,
        boolean direccionPendienteVerificacion,
        String referenciasDireccion
) {
    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(
                p.getId(),
                p.getCotizacion().getId(),
                p.getCotizacion().getProducto().getNombre(),
                p.getCotizacion().getTotal(),
                p.getCotizacion().getDireccion(),
                p.getCotizacion().getComuna(),
                p.getEstado(),
                p.getVisto(),
                p.getCreadoEn(),
                p.getVendedor() == null ? null : p.getVendedor().getId(),
                p.getVendedor() == null ? null : p.getVendedor().getNombre() + " " + p.getVendedor().getApellido(),
                p.getAgendaBloque() == null || p.getAgendaBloque().getTecnico() == null ? null : p.getAgendaBloque().getTecnico().getId(),
                p.getAgendaBloque() == null || p.getAgendaBloque().getTecnico() == null ? null : p.getAgendaBloque().getTecnico().getNombre() + " " + p.getAgendaBloque().getTecnico().getApellido(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getId(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getFecha(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getHoraInicio(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getHoraFin(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getEstado(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getAtencionInicio(),
                p.getAgendaBloque() == null ? null : p.getAgendaBloque().getAtencionFin(),
                p.getCotizacion().requiereVisita(),
                p.getCliente() == null ? null : p.getCliente().getId(),
                Boolean.TRUE.equals(p.getCotizacion().getDireccionPendienteVerificacion()),
                p.getCotizacion().getReferenciasDireccion()
        );
    }
}
