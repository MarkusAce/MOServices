package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import cl.rollerapp.backend.model.enums.EstadoTransaccion;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByTokenWebpay(String tokenWebpay);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.tokenWebpay = :token")
    Optional<Pago> buscarPorTokenParaActualizar(@Param("token") String token);

    List<Pago> findTop100ByEstadoTransaccionAndCreadoEnBeforeOrderByCreadoEnAsc(
            EstadoTransaccion estado, LocalDateTime limite);
    Optional<Pago> findByOrdenCompra(String ordenCompra);
    @Query("select (count(p) > 0) from Pago p where p.estadoTransaccion = :estado "
            + "and (p.cotizacion.id = :cotizacionId or exists (select c.id from Pago p2 join p2.cotizacionesAdicionales c where p2.id = p.id and c.id = :cotizacionId))")
    boolean existsByCotizacion_IdAndEstadoTransaccion(@Param("cotizacionId") Long cotizacionId, @Param("estado") EstadoTransaccion estado);
    List<Pago> findTop100ByEstadoTransaccionOrderByCreadoEnDesc(EstadoTransaccion estado);

    @Query("select (count(p) > 0) from Pago p where p.estadoTransaccion = :estado and p.creadoEn > :desde "
            + "and (p.cotizacion.id = :cotizacionId or exists (select c.id from Pago p2 join p2.cotizacionesAdicionales c where p2.id = p.id and c.id = :cotizacionId))")
    boolean existsByCotizacion_IdAndEstadoTransaccionAndCreadoEnAfter(
            @Param("cotizacionId") Long cotizacionId, @Param("estado") EstadoTransaccion estado, @Param("desde") LocalDateTime desde);
}
