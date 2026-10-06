package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.ObservacionVisita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ObservacionVisitaRepository extends JpaRepository<ObservacionVisita, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update ObservacionVisita o set o.texto = '[contenido eliminado]' "
            + "where o.bloque.cotizacion.cliente.id = :clienteId")
    int anonimizarPorCliente(@org.springframework.data.repository.query.Param("clienteId") Long clienteId);

    Page<ObservacionVisita> findByBloque_IdOrderByCreadoEnDescIdDesc(Long bloqueId, Pageable pagina);
}
