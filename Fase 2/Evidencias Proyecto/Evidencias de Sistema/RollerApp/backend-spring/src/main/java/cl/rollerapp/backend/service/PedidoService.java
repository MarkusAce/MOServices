package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.pedido.PedidoResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.Pedido;
import cl.rollerapp.backend.model.Usuario;
import cl.rollerapp.backend.model.enums.Rol;
import cl.rollerapp.backend.repository.UsuarioRepository;
import cl.rollerapp.backend.model.enums.EstadoPedido;
import cl.rollerapp.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComisionService comisionService;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPropios(Long clienteId) {
        return pedidoRepository.findByCliente_IdOrderByIdDesc(clienteId).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAllByOrderByIdDesc().stream().map(PedidoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMisVisitas(Long tecnicoId, java.time.LocalDate fecha) {
        return pedidoRepository
                .listarVisitasTecnico(tecnicoId, fecha == null ? java.time.LocalDate.now(java.time.ZoneId.of("America/Santiago")) : fecha)
                .stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarNotificacionesPendientes(Long clienteId) {
        return pedidoRepository.findByCliente_IdAndVistoFalse(clienteId).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional
    public int marcarNotificacionesVistas(Long clienteId) {
        List<Pedido> pendientes = pedidoRepository.findByCliente_IdAndVistoFalse(clienteId);
        pendientes.forEach(p -> p.setVisto(true));
        pedidoRepository.saveAll(pendientes);
        return pendientes.size();
    }

    @Transactional
    public PedidoResponse asignarVendedor(Long pedidoId, Long vendedorId) {
        Pedido pedido = pedidoRepository.buscarParaActualizar(pedidoId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));
        if (pedido.getEstado() == EstadoPedido.REALIZADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede asignar vendedor a un pedido finalizado o cancelado.");
        }
        Usuario vendedor = usuarioRepository.findById(vendedorId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendedor no encontrado."));
        if (vendedor.getRol() != Rol.VENDEDOR || !Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe seleccionar un vendedor activo.");
        }
        pedido.setVendedor(vendedor);
        return PedidoResponse.desde(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado, Long actorId, boolean administrador) {
        Pedido pedido = pedidoRepository.buscarParaActualizar(pedidoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));

        
        
        if (!administrador) {
            if (actorId == null || pedido.getAgendaBloque() == null
                    || pedido.getAgendaBloque().getTecnico() == null
                    || !actorId.equals(pedido.getAgendaBloque().getTecnico().getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "El pedido no pertenece a las visitas asignadas al técnico.");
            }
            if (nuevoEstado != EstadoPedido.EN_TERRENO && nuevoEstado != EstadoPedido.REALIZADO) {
                throw new ApiException(HttpStatus.FORBIDDEN, "El técnico solo puede registrar avances de terreno.");
            }
        }

        EstadoPedido actual = pedido.getEstado();
        if (actual == nuevoEstado) {
            return PedidoResponse.desde(pedido); 
        }
        if (actual == EstadoPedido.REALIZADO || actual == EstadoPedido.CANCELADO) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede modificar un pedido finalizado o cancelado.");
        }
        boolean transicionValida = switch (actual) {
            case PAGADO -> nuevoEstado == EstadoPedido.EN_CONFECCION || nuevoEstado == EstadoPedido.CANCELADO;
            case EN_CONFECCION -> nuevoEstado == EstadoPedido.CANCELADO
                    || (pedido.getCotizacion().requiereVisita()
                        ? nuevoEstado == EstadoPedido.EN_TERRENO
                        : nuevoEstado == EstadoPedido.REALIZADO);
            case EN_TERRENO -> nuevoEstado == EstadoPedido.REALIZADO
                    || nuevoEstado == EstadoPedido.REPROGRAMADO || nuevoEstado == EstadoPedido.CANCELADO;
            case REPROGRAMADO -> nuevoEstado == EstadoPedido.EN_TERRENO || nuevoEstado == EstadoPedido.CANCELADO;
            case REALIZADO, CANCELADO -> false;
        };
        if (!transicionValida) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Transición de pedido no permitida: " + actual + " -> " + nuevoEstado + ".");
        }
        if (nuevoEstado == EstadoPedido.REALIZADO && pedido.getCotizacion().requiereVisita()
                && (pedido.getAgendaBloque() == null
                    || pedido.getAgendaBloque().getEstado() != cl.rollerapp.backend.model.enums.EstadoAgenda.COMPLETADO)) {
            throw new ApiException(HttpStatus.CONFLICT, "Completa la visita técnica antes de finalizar el pedido.");
        }
        if (nuevoEstado == EstadoPedido.CANCELADO && pedido.getAgendaBloque() != null
                && pedido.getAgendaBloque().getEstado() != cl.rollerapp.backend.model.enums.EstadoAgenda.COMPLETADO) {
            pedido.getAgendaBloque().setEstado(cl.rollerapp.backend.model.enums.EstadoAgenda.CANCELADO);
            pedido.getAgendaBloque().setReservadoHasta(null);
        }
        if (nuevoEstado == EstadoPedido.EN_TERRENO && Boolean.TRUE.equals(pedido.getCotizacion().getDireccionPendienteVerificacion()))
            throw new ApiException(HttpStatus.CONFLICT, "La administración debe verificar la dirección antes de enviar al técnico.");
        if (nuevoEstado == EstadoPedido.EN_TERRENO && (pedido.getAgendaBloque() == null
                || pedido.getAgendaBloque().getEstado() != cl.rollerapp.backend.model.enums.EstadoAgenda.RESERVADO))
            throw new ApiException(HttpStatus.CONFLICT, "Asigna una visita reservada antes de enviar al técnico.");
        boolean cambioDeVerdad = true;
        pedido.setEstado(nuevoEstado);
        if (cambioDeVerdad) pedido.setVisto(false);

        try {
            Pedido guardado = pedidoRepository.save(pedido);
            if (cambioDeVerdad) {
                eventos.publishEvent(new NotificacionTransaccional(
                        guardado.getCliente().getCorreo(),
                        "Actualización de tu pedido #" + guardado.getId() + " — RollerApp",
                        "Hola " + guardado.getCliente().getNombre() + ",\n\n"
                                + "Tu pedido #" + guardado.getId() + " cambió al estado: "
                                + EstadoPedidoTexto.etiqueta(nuevoEstado) + ".\n"
                                + "Puedes consultar los detalles en RollerApp.\n\nEquipo RollerApp"));
            }
            if (cambioDeVerdad && nuevoEstado == EstadoPedido.REALIZADO) {
                comisionService.generarAlFinalizar(guardado);
            }
            return PedidoResponse.desde(guardado);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La base de datos rechazó ese cambio de estado (regla de negocio RN-04).");
        }
    }
}
