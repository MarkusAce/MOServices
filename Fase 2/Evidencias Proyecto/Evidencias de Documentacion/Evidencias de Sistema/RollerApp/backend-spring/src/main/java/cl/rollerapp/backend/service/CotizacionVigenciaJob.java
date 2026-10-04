package cl.rollerapp.backend.service;

import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import cl.rollerapp.backend.repository.CotizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class CotizacionVigenciaJob {
    private final CotizacionRepository cotizaciones;
    private final CotizacionVigenciaService vigencia;

    @Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
    public void expirarPendientes() {
        cotizaciones.findByEstadoAndCreadoEnBefore(
                        EstadoCotizacion.PENDIENTE_PAGO, LocalDateTime.now().minusDays(15))
                .forEach(c -> vigencia.expirarSiVencida(c.getId()));
    }
}
