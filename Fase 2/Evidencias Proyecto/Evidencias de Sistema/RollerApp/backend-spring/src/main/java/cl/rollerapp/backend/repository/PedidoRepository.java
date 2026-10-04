package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Pedido;
import cl.rollerapp.backend.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    boolean existsByVendedor_IdAndEstadoNotIn(Long vendedorId, java.util.Collection<EstadoPedido> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarParaActualizar(@Param("id") Long id);

    List<Pedido> findByCliente_IdOrderByIdDesc(Long clienteId);
    List<Pedido> findAllByOrderByIdDesc();
    List<Pedido> findByCreadoEnGreaterThanEqual(LocalDateTime desde);
    List<Pedido> findByCliente_IdAndVistoFalse(Long clienteId);
    List<Pedido> findByEstado(EstadoPedido estado);
    long countByEstado(EstadoPedido estado);
    Optional<Pedido> findByCotizacion_Id(Long cotizacionId);
    boolean existsByAgendaBloque_Id(Long agendaBloqueId);

    @Query("select p from Pedido p join fetch p.agendaBloque a join fetch a.tecnico "
            + "join fetch p.cotizacion c join fetch c.producto "
            + "left join fetch p.vendedor where a.tecnico.id = :tecnicoId and a.fecha = :fecha "
            + "order by a.fecha asc, a.horaInicio asc, a.id asc, p.id asc")
    List<Pedido> listarVisitasTecnico(@Param("tecnicoId") Long tecnicoId, @Param("fecha") java.time.LocalDate fecha);
}
