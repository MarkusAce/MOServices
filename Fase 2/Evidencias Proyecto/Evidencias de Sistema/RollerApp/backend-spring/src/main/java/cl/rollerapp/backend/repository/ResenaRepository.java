package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findAllByOrderByFechaDesc();
    List<Resena> findByUsuario_Id(Long usuarioId);
}
