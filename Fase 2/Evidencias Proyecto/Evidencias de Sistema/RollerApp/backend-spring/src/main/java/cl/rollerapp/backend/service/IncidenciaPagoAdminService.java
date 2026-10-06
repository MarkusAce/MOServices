package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.pago.IncidenciaPagoAdminResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.AgendaBloque;
import cl.rollerapp.backend.model.Pago;
import cl.rollerapp.backend.model.Pedido;
import cl.rollerapp.backend.model.enums.EstadoAgenda;
import cl.rollerapp.backend.model.enums.EstadoPedido;
import cl.rollerapp.backend.model.enums.EstadoTransaccion;
import cl.rollerapp.backend.repository.AgendaBloqueRepository;
import cl.rollerapp.backend.repository.PagoRepository;
import cl.rollerapp.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaPagoAdminService {
    private final PagoRepository pagos;
    private final PedidoRepository pedidos;
    private final AgendaBloqueRepository agenda;
    private final AgendaService agendaService;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public List<IncidenciaPagoAdminResponse> listar() {
        List<IncidenciaPagoAdminResponse> resultado = new ArrayList<>();
        for (Pago pago : pagos.findTop100ByEstadoTransaccionOrderByCreadoEnDesc(EstadoTransaccion.AUTORIZADA)) {
            for (var cotizacion : pago.cotizacionesCompra()) {
                Pedido pedido = pedidos.findByCotizacion_Id(cotizacion.getId()).orElse(null);
                if (pedido == null || (pedido.getAgendaBloque() == null
                        && pedido.getEstado() != EstadoPedido.CANCELADO
                        && pedido.getEstado() != EstadoPedido.REALIZADO
                        && pedido.getCotizacion().requiereVisita())) {
                    resultado.add(respuesta(pago, pedido, pedido == null ? "PAGO_SIN_PEDIDO" : "PEDIDO_SIN_VISITA", cotizacion.getId()));
                }
            }
        }
        for (Pago pago : pagos.findTop100ByEstadoTransaccionAndCreadoEnBeforeOrderByCreadoEnAsc(
                EstadoTransaccion.INICIADA, LocalDateTime.now().minusMinutes(30))) {
            resultado.add(respuesta(pago, null, "PAGO_PENDIENTE_CONCILIACION", pago.getCotizacion().getId()));
        }
        return resultado;
    }

    private IncidenciaPagoAdminResponse respuesta(Pago pago, Pedido pedido, String motivo, Long cotizacionId) {
        return new IncidenciaPagoAdminResponse(pago.getId(), cotizacionId,
                pedido == null ? null : pedido.getId(), pago.getOrdenCompra(), pago.getMonto(),
                pago.getEstadoTransaccion().name(), motivo, pago.getCreadoEn());
    }

    @Transactional
    public void reagendar(Long pedidoId, Long bloqueId) {
        Pedido pedido = pedidos.buscarParaActualizar(pedidoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));
        if (pedido.getAgendaBloque() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "El pedido ya tiene una visita. No se modifica automáticamente.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.REALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede reagendar un pedido cerrado.");
        }
        if (!pedido.getCotizacion().requiereVisita()) {
            throw new ApiException(HttpStatus.CONFLICT, "Este pedido no incluye servicios; no requiere asignar una visita.");
        }
        boolean pagoAutorizado = pagos.existsByCotizacion_IdAndEstadoTransaccion(
                pedido.getCotizacion().getId(), EstadoTransaccion.AUTORIZADA);
        if (!pagoAutorizado) {
            throw new ApiException(HttpStatus.CONFLICT, "No se encontró un pago autorizado para este pedido.");
        }
        AgendaBloque bloque = agenda.buscarConBloqueoParaActualizar(bloqueId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Horario no encontrado."));
        if (bloque.getEstado() != EstadoAgenda.DISPONIBLE || bloque.getCotizacion() != null
                || pedidos.existsByAgendaBloque_Id(bloqueId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Horario ocupado o asociado a otro pedido.");
        }
        if (agendaService.listarDisponibles().stream().noneMatch(b -> b.id().equals(bloqueId))) {
            throw new ApiException(HttpStatus.CONFLICT, "El horario no cumple la anticipación mínima de 48 horas hábiles.");
        }
        bloque.setEstado(EstadoAgenda.RESERVADO);
        bloque.setCotizacion(pedido.getCotizacion());
        bloque.setReservadoHasta(null);
        agenda.save(bloque);
        pedido.setAgendaBloque(bloque);
        pedidos.save(pedido);
        eventos.publishEvent(new NotificacionTransaccional(
                pedido.getCliente().getCorreo(),
                "Visita técnica asignada: pedido #" + pedido.getId() + " — RollerApp",
                "Hola " + pedido.getCliente().getNombre() + ",\n\n"
                        + "Se asignó una visita técnica a tu pedido #" + pedido.getId() + ".\n"
                        + "Fecha: " + bloque.getFecha() + ".\n"
                        + "Hora de inicio: " + bloque.getHoraInicio() + ".\n"
                        + "Puedes consultar los detalles en RollerApp.\n\nEquipo RollerApp"));
    }
}
