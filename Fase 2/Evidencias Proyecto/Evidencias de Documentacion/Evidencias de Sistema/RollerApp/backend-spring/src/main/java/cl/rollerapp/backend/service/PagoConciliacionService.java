package cl.rollerapp.backend.service;

import cl.rollerapp.backend.model.Pago;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.enums.EstadoTransaccion;
import cl.rollerapp.backend.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class PagoConciliacionService {
    private final PagoRepository pagos;
    private final PagoService pagoService;
    private final WebpayService webpay;

    
    public String revisarPago(Long pagoId) {
        Pago pago = pagos.findById(pagoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pago no encontrado."));
        if (pago.getEstadoTransaccion() != EstadoTransaccion.INICIADA) {
            return "Este pago ya fue procesado. Actualiza las incidencias.";
        }
        if (pago.getTokenWebpay() == null || pago.getTokenWebpay().isBlank()) {
            return "Pago sin token: requiere revisión manual con Transbank.";
        }
        var remoto = webpay.consultarEstado(pago.getTokenWebpay());
        if ("AUTHORIZED".equalsIgnoreCase(remoto.estado())
                && Integer.valueOf(0).equals(remoto.codigoRespuesta())) {
            var respuesta = pagoService.confirmarRetornoWebpay(pago.getTokenWebpay());
            return respuesta.pedidoId() == null
                    ? "Pago autorizado sin pedido: requiere revisión manual. No intentes otro cobro."
                    : "Pago conciliado. Pedido #" + respuesta.pedidoId() + ".";
        }
        return "Estado en Webpay: " + (remoto.estado() == null ? "sin información" : remoto.estado())
                + ". No se realizó ningún cobro ni cambio de estado.";
    }

    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void conciliarPagosInterrumpidos() {
        
        var pendientes = pagos.findTop100ByEstadoTransaccionAndCreadoEnBeforeOrderByCreadoEnAsc(
                EstadoTransaccion.INICIADA, LocalDateTime.now().minusMinutes(30));
        for (Pago pago : pendientes) {
            if (pago.getTokenWebpay() == null || pago.getTokenWebpay().isBlank()) {
                log.warn("Pago {} sin token; requiere revisión manual.", pago.getId());
                continue;
            }
            try {
                var remoto = webpay.consultarEstado(pago.getTokenWebpay());
                if ("AUTHORIZED".equalsIgnoreCase(remoto.estado())
                        && Integer.valueOf(0).equals(remoto.codigoRespuesta())) {
                    
                    var respuesta = pagoService.confirmarPago(
                            pago.getCotizacion().getCliente().getId(), pago.getTokenWebpay());
                    if (respuesta.pedidoId() == null) {
                        log.error("Pago {} autorizado sin pedido: requiere revisión inmediata.", pago.getId());
                    } else {
                        log.info("Pago {} conciliado con pedido {}.", pago.getId(), respuesta.pedidoId());
                    }
                } else {
                    
                    
                    log.warn("Pago {} pendiente de revisión; estado remoto {}.", pago.getId(), remoto.estado());
                }
            } catch (Exception ex) {
                log.error("No se pudo conciliar pago {}. Se reintentará en próximo ciclo.", pago.getId(), ex);
            }
        }
    }
}
