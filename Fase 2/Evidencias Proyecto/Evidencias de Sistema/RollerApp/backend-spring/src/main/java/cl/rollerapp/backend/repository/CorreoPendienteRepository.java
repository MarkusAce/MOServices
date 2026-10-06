package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.CorreoPendiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CorreoPendienteRepository extends JpaRepository<CorreoPendiente, Long> {
    List<CorreoPendiente> findTop100ByOrderByCreadoEnDesc();
    List<CorreoPendiente> findTop100ByEstadoAndProximoIntentoEnBeforeOrderByIdAsc(String estado, LocalDateTime limite);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CorreoPendiente c where c.id = :id")
    Optional<CorreoPendiente> buscarParaEnviar(Long id);
    long deleteByEstadoAndEnviadoEnBefore(String estado, LocalDateTime limite);
}
