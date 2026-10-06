package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Comision;
import cl.rollerapp.backend.model.enums.EstadoComision;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ComisionRepository extends JpaRepository<Comision, Long> {
    boolean existsByPedido_IdAndUsuario_Id(Long pedidoId, Long usuarioId);

    @Query("select c from Comision c join fetch c.pedido join fetch c.usuario " +
           "where (:estado is null or c.estado = :estado) and (:usuarioId is null or c.usuario.id = :usuarioId) order by c.id desc")
    List<Comision> listar(@Param("estado") EstadoComision estado, @Param("usuarioId") Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comision c join fetch c.pedido join fetch c.usuario where c.id = :id")
    Optional<Comision> buscarParaLiquidar(@Param("id") Long id);
}
