package cl.rollerapp.backend.service;

import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.Cotizacion;
import cl.rollerapp.backend.model.enums.EstadoAgenda;
import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import cl.rollerapp.backend.repository.AgendaBloqueRepository;
import cl.rollerapp.backend.repository.CotizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class CotizacionVigenciaService {
    private final CotizacionRepository cotizaciones;
    private final AgendaBloqueRepository agenda;

    static boolean estaVencida(Cotizacion cotizacion, LocalDateTime ahora) {
        return cotizacion.getEstado() == EstadoCotizacion.PENDIENTE_PAGO
                && cotizacion.getCreadoEn() != null
                && !cotizacion.getCreadoEn().plusDays(15).isAfter(ahora);
    }

    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expirarPendientesCliente(Long clienteId) {
        cotizaciones.findByCliente_IdOrderByIdDesc(clienteId).stream()
                .filter(c -> estaVencida(c, LocalDateTime.now()))
                .forEach(c -> expirarSiVencida(c.getId()));
    }

    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean expirarSiVencida(Long cotizacionId) {
        Cotizacion cotizacion = cotizaciones.buscarConBloqueoParaActualizar(cotizacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));
        if (!estaVencida(cotizacion, LocalDateTime.now())) return false;
        cotizacion.setEstado(EstadoCotizacion.EXPIRADA);
        cotizaciones.save(cotizacion);
        
        agenda.findByCotizacion_Id(cotizacionId).stream()
                .filter(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO)
                .forEach(b -> {
                    b.setEstado(EstadoAgenda.DISPONIBLE);
                    b.setCotizacion(null);
                    b.setReservadoHasta(null);
                    agenda.save(b);
                });
        return true;
    }
}
