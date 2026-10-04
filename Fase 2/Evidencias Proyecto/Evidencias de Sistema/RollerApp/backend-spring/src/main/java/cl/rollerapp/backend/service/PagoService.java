package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.pago.ConfirmarPagoResponse;
import cl.rollerapp.backend.dto.pago.IniciarPagoResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.*;
import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import cl.rollerapp.backend.model.enums.EstadoTransaccion;
import cl.rollerapp.backend.model.enums.EstadoAgenda;
import cl.rollerapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final PedidoRepository pedidoRepository;
    private final AgendaBloqueRepository agendaBloqueRepository;
    private final WebpayService webpayService;
    private final AgendaService agendaService;
    private final CotizacionVigenciaService vigenciaService;
    private final ApplicationEventPublisher eventos;

    @Value("${app.correo.empresa:}")
    private String correoEmpresa;

    @Transactional
    public IniciarPagoResponse iniciarPago(Long clienteId, Long cotizacionId) {
        
        
        if (vigenciaService.expirarSiVencida(cotizacionId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La cotización expiró tras 15 días. Recálcúlala con las tarifas vigentes antes de pagar.");
        }
        Cotizacion cotizacion = cotizacionRepository.buscarConBloqueoParaActualizar(cotizacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));

        if (!cotizacion.getCliente().getId().equals(clienteId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta cotización no te pertenece.");
        }
        if (cotizacion.getEstado() != EstadoCotizacion.PENDIENTE_PAGO) {
            throw new ApiException(HttpStatus.CONFLICT, "Esta cotización ya no está pendiente de pago.");
        }

        
        if (pagoRepository.existsByCotizacion_IdAndEstadoTransaccionAndCreadoEnAfter(
                cotizacionId, EstadoTransaccion.INICIADA, LocalDateTime.now().minusMinutes(15))) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Ya existe un pago en curso para esta cotización. Completa o recupera ese pago antes de iniciar otro.");
        }

        if (cotizacion.requiereVisita()) {
            
            AgendaBloque bloqueCheckout = agendaBloqueRepository.findByCotizacion_Id(cotizacionId).stream()
                    .filter(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO
                            && b.getReservadoHasta() != null && b.getReservadoHasta().isAfter(LocalDateTime.now()))
                    .max(Comparator.comparing(AgendaBloque::getReservadoHasta))
                    .flatMap(b -> agendaBloqueRepository.buscarConBloqueoParaActualizar(b.getId()))
                    .filter(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO
                            && b.getReservadoHasta() != null && b.getReservadoHasta().isAfter(LocalDateTime.now()))
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                            "La reserva expiró. Elige un nuevo horario antes de pagar."));
        }

        String ordenCompra = "ROLLER-" + cotizacion.getId() + "-" + System.currentTimeMillis() % 1_000_000;
        String sessionId = UUID.randomUUID().toString();

        
        BigDecimal montoCobro = cotizacion.getTotal().setScale(0, RoundingMode.HALF_UP);
        WebpayService.RespuestaCrearTransaccion respuesta = webpayService.crearTransaccion(
                ordenCompra, sessionId, montoCobro);

        Pago pago = Pago.builder()
                .cotizacion(cotizacion)
                .tokenWebpay(respuesta.token())
                .ordenCompra(ordenCompra)
                .monto(montoCobro)
                .estadoTransaccion(EstadoTransaccion.INICIADA)
                .build();
        pagoRepository.save(pago);

        

        return new IniciarPagoResponse(respuesta.url(), respuesta.token());
    }

    
    @Transactional
    public IniciarPagoResponse iniciarCarrito(Long clienteId,
            cl.rollerapp.backend.dto.pago.IniciarCarritoRequest req) {
        var items = req.items();
        if (items == null || items.isEmpty() || items.size() > 20
                || items.stream().anyMatch(i -> i == null || i.cotizacionId() == null || i.cotizacionId() <= 0)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito debe contener entre 1 y 20 cortinas.");
        }
        if (items.stream().map(i -> i.cotizacionId()).distinct().count() != items.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Una cotización no puede repetirse en el carrito.");
        }
        var bloques = items.stream().map(i -> i.bloqueId()).filter(java.util.Objects::nonNull).toList();
        if (bloques.stream().distinct().count() != bloques.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selecciona un horario diferente por cortina con servicios.");
        }
        
        for (var item : items) {
            var c = cotizacionRepository.findById(item.cotizacionId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));
            if (!c.getCliente().getId().equals(clienteId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Una cotización del carrito no te pertenece.");
            }
            if (vigenciaService.expirarSiVencida(c.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "Hay una cotización vencida. Renueva el carrito antes de pagar.");
            }
        }
        
        List<Cotizacion> compra = new java.util.ArrayList<>();
        for (var item : items.stream().sorted(Comparator.comparing(i -> i.cotizacionId())).toList()) {
            var c = cotizacionRepository.buscarConBloqueoParaActualizar(item.cotizacionId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));
            if (!c.getCliente().getId().equals(clienteId) || c.getEstado() != EstadoCotizacion.PENDIENTE_PAGO) {
                throw new ApiException(HttpStatus.CONFLICT, "Una cotización ya no está disponible para esta compra.");
            }
            if (pagoRepository.existsByCotizacion_IdAndEstadoTransaccionAndCreadoEnAfter(
                    c.getId(), EstadoTransaccion.INICIADA, LocalDateTime.now().minusMinutes(15))) {
                throw new ApiException(HttpStatus.CONFLICT, "Hay un pago en curso. Revisa Mis pedidos antes de reintentar.");
            }
            if (c.getTotal() == null || c.getTotal().signum() < 0) {
                throw new ApiException(HttpStatus.CONFLICT, "La cotización no tiene un importe válido.");
            }
            if (!c.requiereVisita() && item.bloqueId() != null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Una cortina sin servicios no necesita horario.");
            }
            compra.add(c);
        }
        
        for (var item : items.stream().filter(i -> i.bloqueId() != null)
                .sorted(Comparator.comparing(i -> i.bloqueId())).toList()) {
            agendaService.preReservarVigente(clienteId, item.bloqueId(), item.cotizacionId());
        }
        for (var c : compra) {
            if (c.requiereVisita() && agendaBloqueRepository.findByCotizacion_Id(c.getId()).stream()
                    .noneMatch(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO && b.getReservadoHasta() != null
                            && b.getReservadoHasta().isAfter(LocalDateTime.now()))) {
                throw new ApiException(HttpStatus.CONFLICT, "Selecciona un horario para cada cortina con servicios.");
            }
        }
        BigDecimal monto = compra.stream().map(c -> c.getTotal().setScale(0, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (monto.signum() <= 0 || monto.compareTo(new BigDecimal("99999999")) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El importe del carrito debe estar entre $1 y $99.999.999.");
        }
        String orden = "RC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        var respuesta = webpayService.crearTransaccion(orden, UUID.randomUUID().toString(), monto);
        pagoRepository.save(Pago.builder().cotizacion(compra.get(0))
                .cotizacionesAdicionales(new java.util.ArrayList<>(compra.subList(1, compra.size())))
                .tokenWebpay(respuesta.token()).ordenCompra(orden).monto(monto)
                .estadoTransaccion(EstadoTransaccion.INICIADA).build());
        return new IniciarPagoResponse(respuesta.url(), respuesta.token());
    }

    @Transactional
    public ConfirmarPagoResponse confirmarPago(Long clienteId, String tokenWebpay) {
        return procesarPago(clienteId, tokenWebpay);
    }

    
    @Transactional
    public ConfirmarPagoResponse confirmarRetornoWebpay(String tokenWebpay) {
        return procesarPago(null, tokenWebpay);
    }

    private ConfirmarPagoResponse procesarPago(Long clienteId, String tokenWebpay) {
        if (tokenWebpay == null || tokenWebpay.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Falta el token de Webpay.");
        }
        Pago pago = pagoRepository.buscarPorTokenParaActualizar(tokenWebpay)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró el pago iniciado."));

        Cotizacion principal = pago.getCotizacion();
        if (clienteId != null && !principal.getCliente().getId().equals(clienteId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Este pago no te pertenece.");
        }

        if (pago.getEstadoTransaccion() == EstadoTransaccion.AUTORIZADA) {
            var existentes = pago.cotizacionesCompra().stream()
                    .map(c -> pedidoRepository.findByCotizacion_Id(c.getId()).orElse(null))
                    .filter(java.util.Objects::nonNull).map(Pedido::getId).toList();
            return new ConfirmarPagoResponse(true, "AUTHORIZED", pago.getOrdenCompra(), pago.getMonto(),
                    pago.getCodigoAutorizacion(), pago.getTarjetaUltimosDigitos(),
                    existentes.isEmpty() ? null : existentes.get(0), existentes, cotizacionIds(pago));
        }
        if (pago.getEstadoTransaccion() == EstadoTransaccion.ANULADA) return new ConfirmarPagoResponse(false,"ABORTED",pago.getOrdenCompra(),pago.getMonto(),null,null,null,java.util.List.of(),cotizacionIds(pago));
        if (pago.getEstadoTransaccion() == EstadoTransaccion.RECHAZADA) {
            return new ConfirmarPagoResponse(false,"FAILED",pago.getOrdenCompra(),pago.getMonto(),
                    pago.getCodigoAutorizacion(),pago.getTarjetaUltimosDigitos(),null,java.util.List.of(),cotizacionIds(pago));
        }
        if (pago.getEstadoTransaccion() != EstadoTransaccion.INICIADA) {
            throw new ApiException(HttpStatus.CONFLICT, "Esta transacción ya fue procesada.");
        }
        
        
        WebpayService.RespuestaConfirmarTransaccion estadoActual = webpayService.consultarEstado(tokenWebpay);
        WebpayService.RespuestaConfirmarTransaccion resultado = java.util.Set.of("AUTHORIZED", "FAILED", "REVERSED", "NULLIFIED", "ABORTED").contains(
                    estadoActual.estado() == null ? "" : estadoActual.estado().toUpperCase(java.util.Locale.ROOT))
                ? estadoActual : webpayService.confirmarTransaccion(tokenWebpay);
        if (!pago.getOrdenCompra().equals(resultado.ordenCompra())
                || resultado.monto() == null || pago.getMonto().compareTo(resultado.monto()) != 0) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Los datos de la transacción no coinciden con la cotización.");
        }
        boolean aprobado = "AUTHORIZED".equalsIgnoreCase(resultado.estado())
                && Integer.valueOf(0).equals(resultado.codigoRespuesta());

        pago.setEstadoTransaccion(aprobado ? EstadoTransaccion.AUTORIZADA : EstadoTransaccion.RECHAZADA);
        pago.setCodigoAutorizacion(resultado.codigoAutorizacion());
        pago.setTipoPago(resultado.tipoPago());
        pago.setTarjetaUltimosDigitos(resultado.ultimosDigitosTarjeta());
        pago.setFechaTransaccion(LocalDateTime.now());
        pagoRepository.save(pago);

        Long pedidoId = null;
        List<Long> pedidoIds = new java.util.ArrayList<>();
        for (Cotizacion cotizacion : pago.cotizacionesCompra()) {
            List<AgendaBloque> bloques = agendaBloqueRepository.findByCotizacion_Id(cotizacion.getId());

            if (aprobado) {
                cotizacion.setEstado(EstadoCotizacion.PAGADA);
                cotizacionRepository.save(cotizacion);

                
                
                AgendaBloque bloque = !cotizacion.requiereVisita() ? null : bloques.stream()
                        .filter(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO)
                        .filter(b -> b.getReservadoHasta() != null && b.getReservadoHasta().isAfter(LocalDateTime.now()))
                        .max(Comparator.comparing(AgendaBloque::getReservadoHasta)
                                .thenComparing(AgendaBloque::getId)).orElse(null);
                bloques.stream().filter(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO && b != bloque)
                        .forEach(b -> liberarBloque(b));
                if (bloque != null) {
                    agendaService.confirmarReserva(bloque.getId());
                }

                if (bloque == null && cotizacion.requiereVisita()) {
                    log.warn("Pago autorizado para cotización {} sin visita vigente; requiere reagendamiento.", cotizacion.getId());
                }
                Pedido pedido = Pedido.builder()
                        .cotizacion(cotizacion)
                        .cliente(cotizacion.getCliente())
                        .agendaBloque(bloque)
                        .build();
                pedidoId = pedidoRepository.save(pedido).getId();
                pedidoIds.add(pedidoId);
                eventos.publishEvent(new NotificacionTransaccional(
                        cotizacion.getCliente().getCorreo(),
                        "Pago confirmado: pedido #" + pedidoId + " — RollerApp",
                        "Hola " + cotizacion.getCliente().getNombre() + ",\n\n"
                                + "Confirmamos el pago de tu pedido #" + pedidoId + ".\n"
                                + "Orden de compra: " + pago.getOrdenCompra() + "\n"
                                + "Código de autorización: " + resultado.codigoAutorizacion() + "\n"
                                + "Monto: $" + (cotizacion.getTotal() == null ? pago.getMonto() : cotizacion.getTotal().setScale(0, RoundingMode.HALF_UP)).toPlainString() + " CLP.\n"
                                + (!cotizacion.requiereVisita()
                                    ? "Tu compra no incluye servicios ni visita técnica.\n"
                                    : bloque == null
                                    ? "Tu visita técnica está pendiente de asignación; te contactaremos para coordinarla.\n"
                                    : "Tu visita técnica quedó reservada. Revisa sus detalles en RollerApp.\n")
                                + "\nEquipo RollerApp"));
                if (correoEmpresa != null && !correoEmpresa.isBlank()) {
                    eventos.publishEvent(new NotificacionTransaccional(correoEmpresa,
                            "Comprobante de pago: pedido #" + pedidoId + " — RollerApp",
                            "Pedido #" + pedidoId + "\nOrden: " + pago.getOrdenCompra()
                                    + "\nMonto de la cortina: $" + (cotizacion.getTotal() == null ? pago.getMonto() : cotizacion.getTotal().setScale(0, RoundingMode.HALF_UP)).toPlainString() + " CLP"
                                    + "\nCliente: " + cotizacion.getCliente().getCorreo()));
                }

            } else {
                bloques.stream().filter(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO)
                        .forEach(this::liberarBloque);
            }

        }
        return new ConfirmarPagoResponse(
                aprobado,
                resultado.estado(),
                resultado.ordenCompra(),
                resultado.monto(),
                resultado.codigoAutorizacion(),
                resultado.ultimosDigitosTarjeta(),
                pedidoIds.isEmpty() ? null : pedidoIds.get(0),
                pedidoIds,
                cotizacionIds(pago)
        );
    }

    @Transactional
    public ConfirmarPagoResponse anularRetornoWebpay(String token) {
        Pago pago = pagoRepository.buscarPorTokenParaActualizar(token)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pago no encontrado."));
        if (pago.getEstadoTransaccion() != EstadoTransaccion.INICIADA) return procesarPago(null, token);
        var remoto = webpayService.consultarEstado(token);
        if ("AUTHORIZED".equalsIgnoreCase(remoto.estado())) {
            return procesarPago(null, token);
        }
        if (!java.util.Set.of("INITIALIZED", "ABORTED", "FAILED", "REVERSED", "NULLIFIED").contains(remoto.estado()))
            throw new ApiException(HttpStatus.CONFLICT, "El pago requiere verificación antes de liberar la reserva.");
        if (!pago.getOrdenCompra().equals(remoto.ordenCompra()) || remoto.monto() == null || pago.getMonto().compareTo(remoto.monto()) != 0)
            throw new ApiException(HttpStatus.BAD_GATEWAY, "La transacción no coincide con el pago.");
        pago.setEstadoTransaccion(EstadoTransaccion.ANULADA);
        pago.setFechaTransaccion(LocalDateTime.now());
        for (Cotizacion c : pago.cotizacionesCompra().stream().sorted(Comparator.comparing(Cotizacion::getId)).toList()) {
            cotizacionRepository.buscarConBloqueoParaActualizar(c.getId());
            for (AgendaBloque original : agendaBloqueRepository.findByCotizacion_Id(c.getId()).stream().sorted(Comparator.comparing(AgendaBloque::getId)).toList()) {
                AgendaBloque bloque = agendaBloqueRepository.buscarConBloqueoParaActualizar(original.getId()).orElseThrow();
                if (bloque.getEstado() == EstadoAgenda.PRE_RESERVADO && bloque.getCotizacion() != null && bloque.getCotizacion().getId().equals(c.getId())) liberarBloque(bloque);
            }
        }
        pagoRepository.save(pago);
        return new ConfirmarPagoResponse(false,"ABORTED",pago.getOrdenCompra(),pago.getMonto(),null,null,null,java.util.List.of(),cotizacionIds(pago));
    }

    private java.util.List<Long> cotizacionIds(Pago pago) {
        return pago.cotizacionesCompra().stream().map(Cotizacion::getId).toList();
    }

    private void liberarBloque(AgendaBloque bloque) {
        bloque.setEstado(EstadoAgenda.DISPONIBLE);
        bloque.setCotizacion(null);
        bloque.setReservadoHasta(null);
        agendaBloqueRepository.save(bloque);
    }
}
