package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Cotizacion;
import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cotizacion c WHERE c.id = :id")
    Optional<Cotizacion> buscarConBloqueoParaActualizar(Long id);
    List<Cotizacion> findByCliente_IdOrderByIdDesc(Long clienteId);
    Page<Cotizacion> findAllByOrderByIdDesc(Pageable pageable);

    List<Cotizacion> findByCreadoEnGreaterThanEqual(LocalDateTime desde);

    List<Cotizacion> findByEstadoAndCreadoEnBefore(EstadoCotizacion estado, LocalDateTime limite);
}
