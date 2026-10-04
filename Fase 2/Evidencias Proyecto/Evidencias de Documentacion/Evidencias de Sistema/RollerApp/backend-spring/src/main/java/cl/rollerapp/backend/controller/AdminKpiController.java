package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.model.Pedido;
import cl.rollerapp.backend.model.Cotizacion;
import cl.rollerapp.backend.model.Usuario;
import cl.rollerapp.backend.model.enums.EstadoPedido;
import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import cl.rollerapp.backend.repository.PedidoRepository;
import cl.rollerapp.backend.repository.CotizacionRepository;
import cl.rollerapp.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminKpiController {
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final cl.rollerapp.backend.repository.AgendaBloqueRepository agenda;
    private final CotizacionRepository cotizacionRepository;

    public record MesKpi(String mes, BigDecimal ventas, long compras, long usuarios, long cotizaciones, long concretadas) {}
    public record Kpis(BigDecimal ventasMes, long comprasMes, long usuariosMes,
                       long enConfeccion, long enTerreno, long cotizacionesMes, long cotizacionesPendientesMes, LocalDateTime actualizadoEn,
                       List<MesKpi> meses, long cotizacionesConcretadasMes, BigDecimal conversionPorcentaje,
                       long visitasAgendadas, long visitasCompletadas, long visitasCanceladas, Double tiempoPromedioAtencionMinutos, long visitasConDuracion) {}

    @GetMapping("/kpis")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Kpis obtener() {
        YearMonth actual = YearMonth.now();
        YearMonth primero = actual.minusMonths(5);
        List<Pedido> pedidos = pedidoRepository.findByCreadoEnGreaterThanEqual(primero.atDay(1).atStartOfDay());
        List<Cotizacion> cotizaciones = cotizacionRepository.findByCreadoEnGreaterThanEqual(primero.atDay(1).atStartOfDay());
        List<Usuario> usuarios = usuarioRepository.findByCreadoEnGreaterThanEqual(primero.atDay(1).atStartOfDay());
        List<MesKpi> meses = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            YearMonth mes = primero.plusMonths(i);
            BigDecimal ventas = BigDecimal.ZERO;
            long compras = 0;
            for (Pedido pedido : pedidos) {
                if (pedido.getEstado() != EstadoPedido.CANCELADO && YearMonth.from(pedido.getCreadoEn()).equals(mes)) {
                    compras++;
                    ventas = ventas.add(pedido.getCotizacion().getTotal());
                }
            }
            long nuevos = usuarios.stream().filter(u -> YearMonth.from(u.getCreadoEn()).equals(mes)).count();
            long generadas = cotizaciones.stream().filter(c -> YearMonth.from(c.getCreadoEn()).equals(mes)).count();
            long concretadas = cotizaciones.stream().filter(c -> YearMonth.from(c.getCreadoEn()).equals(mes)
                    && c.getEstado() == EstadoCotizacion.PAGADA).count();
            meses.add(new MesKpi(mes.toString(), ventas, compras, nuevos, generadas, concretadas));
        }
        MesKpi esteMes = meses.get(5);
        long confeccion = pedidoRepository.countByEstado(EstadoPedido.EN_CONFECCION);
        long terreno = pedidoRepository.countByEstado(EstadoPedido.EN_TERRENO);
        long pendientes = cotizaciones.stream().filter(c -> YearMonth.from(c.getCreadoEn()).equals(actual)
                && c.getEstado() == EstadoCotizacion.PENDIENTE_PAGO).count();
        var duraciones = agenda.findAll().stream().filter(b -> b.getEstado() == cl.rollerapp.backend.model.enums.EstadoAgenda.COMPLETADO && b.getAtencionInicio() != null && b.getAtencionFin() != null && !b.getAtencionFin().isBefore(b.getAtencionInicio()))
            .mapToDouble(b -> java.time.Duration.between(b.getAtencionInicio(), b.getAtencionFin()).toMillis() / 60000.0).summaryStatistics();
        return new Kpis(esteMes.ventas(), esteMes.compras(), esteMes.usuarios(),
                confeccion, terreno, esteMes.cotizaciones(), pendientes, LocalDateTime.now(), meses,
                esteMes.concretadas(), esteMes.cotizaciones() == 0 ? BigDecimal.ZERO :
                    BigDecimal.valueOf(esteMes.concretadas()).multiply(BigDecimal.valueOf(100))
                      .divide(BigDecimal.valueOf(esteMes.cotizaciones()),2,java.math.RoundingMode.HALF_UP),
                agenda.countByEstado(cl.rollerapp.backend.model.enums.EstadoAgenda.RESERVADO),
                agenda.countByEstado(cl.rollerapp.backend.model.enums.EstadoAgenda.COMPLETADO),
                agenda.countByEstadoAndCotizacionIsNotNull(cl.rollerapp.backend.model.enums.EstadoAgenda.CANCELADO), duraciones.getCount() == 0 ? null : Math.round(duraciones.getAverage() * 100.0) / 100.0, duraciones.getCount());
    }
}
