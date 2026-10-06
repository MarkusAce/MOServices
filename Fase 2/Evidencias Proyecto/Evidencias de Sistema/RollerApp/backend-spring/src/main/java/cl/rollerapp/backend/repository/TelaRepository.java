package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Tela;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelaRepository extends JpaRepository<Tela, Long> {
    List<Tela> findByActivoTrue();
}
