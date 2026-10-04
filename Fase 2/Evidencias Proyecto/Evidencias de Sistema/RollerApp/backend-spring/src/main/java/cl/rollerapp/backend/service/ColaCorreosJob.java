package cl.rollerapp.backend.service;

import cl.rollerapp.backend.repository.CorreoPendienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component @RequiredArgsConstructor
public class ColaCorreosJob {
    private final CorreoPendienteRepository correos;
    private final ColaCorreosService cola;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void reintentar() {
        correos.findTop100ByEstadoAndProximoIntentoEnBeforeOrderByIdAsc("PENDIENTE", LocalDateTime.now().plusNanos(1))
                .forEach(c -> cola.intentar(c.getId()));
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "America/Santiago")
    @Transactional
    public void depurarEnviados() {
        correos.deleteByEstadoAndEnviadoEnBefore("ENVIADO", LocalDateTime.now().minusDays(30));
    }
}
