package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.AuditoriaAdministrativa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaAdministrativaRepository extends JpaRepository<AuditoriaAdministrativa, Long> {
    Page<AuditoriaAdministrativa> findAllByOrderByCreadoEnDescIdDesc(Pageable pageable);
}
