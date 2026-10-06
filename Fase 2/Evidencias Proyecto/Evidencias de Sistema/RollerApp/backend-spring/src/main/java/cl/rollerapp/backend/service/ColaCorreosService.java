package cl.rollerapp.backend.service;

import cl.rollerapp.backend.model.CorreoPendiente;
import cl.rollerapp.backend.repository.CorreoPendienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor @Slf4j
public class ColaCorreosService {
    private final CorreoPendienteRepository correos;
    private final CorreoService remitente;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long registrar(NotificacionTransaccional mensaje) {
        return correos.save(CorreoPendiente.builder()
                .destinatario(mensaje.destinatario()).asunto(mensaje.asunto())
                .contenido(mensaje.contenido()).estado("PENDIENTE")
                .intentos(0).proximoIntentoEn(LocalDateTime.now())
                .creadoEn(LocalDateTime.now()).build()).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reactivar(Long id) {
        var c = correos.buscarParaEnviar(id).orElseThrow(() -> new cl.rollerapp.backend.exception.ApiException(org.springframework.http.HttpStatus.NOT_FOUND,"Correo no encontrado."));
        if ("ENVIADO".equals(c.getEstado())) throw new cl.rollerapp.backend.exception.ApiException(org.springframework.http.HttpStatus.CONFLICT,"El correo ya fue enviado; no se duplicará.");
        c.setEstado("PENDIENTE");c.setIntentos(0);c.setProximoIntentoEn(LocalDateTime.now());correos.save(c);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void intentar(Long id) {
        var correo = correos.buscarParaEnviar(id).orElse(null);
        if (correo == null || !"PENDIENTE".equals(correo.getEstado())
                || correo.getProximoIntentoEn().isAfter(LocalDateTime.now())) return;
        try {
            if (remitente.enviar(correo.getDestinatario(), correo.getAsunto(), correo.getContenido())) {
                correo.setEstado("ENVIADO");
                correo.setEnviadoEn(LocalDateTime.now());
            } else {
                
                correo.setProximoIntentoEn(LocalDateTime.now().plusHours(1));
            }
        } catch (RuntimeException ex) {
            int intentos = correo.getIntentos() + 1;
            correo.setIntentos(intentos);
            correo.setEstado(intentos >= 5 ? "FALLIDO" : "PENDIENTE");
            correo.setProximoIntentoEn(LocalDateTime.now().plusMinutes(5L * (1L << Math.min(intentos, 5))));
            log.warn("Correo {} no pudo enviarse, intento {}", id, intentos, ex);
        }
        correos.save(correo);
    }
}
