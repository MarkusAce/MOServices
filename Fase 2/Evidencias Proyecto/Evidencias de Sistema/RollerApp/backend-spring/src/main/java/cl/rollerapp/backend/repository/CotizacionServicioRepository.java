package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.CotizacionServicio;
import cl.rollerapp.backend.model.CotizacionServicioId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionServicioRepository extends JpaRepository<CotizacionServicio, CotizacionServicioId> {
    java.util.List<CotizacionServicio> findByCotizacion_Id(Long cotizacionId);
}
