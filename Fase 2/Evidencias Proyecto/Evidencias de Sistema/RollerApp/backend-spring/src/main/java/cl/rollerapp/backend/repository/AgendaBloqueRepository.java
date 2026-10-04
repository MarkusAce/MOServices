package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.AgendaBloque;
import cl.rollerapp.backend.model.enums.EstadoAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendaBloqueRepository extends JpaRepository<AgendaBloque, Long> {
    List<AgendaBloque> findByEstadoAndFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(
            EstadoAgenda estado, LocalDate desde);

    List<AgendaBloque> findByCotizacion_Id(Long cotizacionId);
    List<AgendaBloque> findByTecnico_IdAndFechaBetween(Long tecnicoId, LocalDate desde, LocalDate hasta);
    List<AgendaBloque> findByFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(LocalDate fecha);

    boolean existsByTecnico_IdAndEstadoIn(Long tecnicoId, java.util.Collection<EstadoAgenda> estados);

    List<AgendaBloque> findByCotizacion_Cliente_IdOrderByFechaDescHoraInicioDesc(Long clienteId);
    long countByEstado(EstadoAgenda estado);
    long countByEstadoAndCotizacionIsNotNull(EstadoAgenda estado);

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AgendaBloque a WHERE a.estado = :estado AND a.reservadoHasta <= :ahora ORDER BY a.id")
    List<AgendaBloque> buscarVencidosParaLiberar(EstadoAgenda estado, LocalDateTime ahora);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AgendaBloque a WHERE a.id = :id")
    Optional<AgendaBloque> buscarConBloqueoParaActualizar(Long id);
}
