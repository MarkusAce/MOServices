package cl.rollerapp.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionTransaccionalListener {
    private final ColaCorreosService cola;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarTrasConfirmacion(NotificacionTransaccional evento) {
        if (evento.destinatario() == null || evento.destinatario().isBlank()) return;
        try {
            Long id = cola.registrar(evento);
            cola.intentar(id);
        } catch (RuntimeException ex) {
            
            log.error("No se pudo enviar notificación transaccional; asunto={}", evento.asunto(), ex);
        }
    }
}
