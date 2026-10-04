package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Mecanismo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MecanismoRepository extends JpaRepository<Mecanismo, Long> {
    List<Mecanismo> findByActivoTrue();
}
