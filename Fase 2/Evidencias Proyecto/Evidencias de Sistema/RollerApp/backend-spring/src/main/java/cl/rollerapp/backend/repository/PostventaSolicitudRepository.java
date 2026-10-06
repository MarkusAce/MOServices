package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.PostventaSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostventaSolicitudRepository extends JpaRepository<PostventaSolicitud, Long> {
    List<PostventaSolicitud> findAllByOrderByFechaDesc();
    List<PostventaSolicitud> findByUsuario_Id(Long usuarioId);
}
