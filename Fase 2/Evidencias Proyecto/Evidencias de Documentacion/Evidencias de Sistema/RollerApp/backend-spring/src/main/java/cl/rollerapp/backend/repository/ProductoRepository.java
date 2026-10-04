package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Producto;
import cl.rollerapp.backend.model.enums.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();
    List<Producto> findByActivoTrueAndCategoria(Categoria categoria);
}
