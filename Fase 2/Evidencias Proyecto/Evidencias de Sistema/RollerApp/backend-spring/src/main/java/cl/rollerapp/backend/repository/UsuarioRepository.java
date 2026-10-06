package cl.rollerapp.backend.repository;

import cl.rollerapp.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import cl.rollerapp.backend.model.enums.Rol;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    List<Usuario> findByCreadoEnGreaterThanEqual(LocalDateTime desde);
    List<Usuario> findByRolAndAnonimizadoEnIsNullAndUltimoAccesoEnBeforeOrderByIdAsc(Rol rol, LocalDateTime limite);

    boolean existsByRolAndActivoTrueAndIdNot(Rol rol, Long id);

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> buscarTecnicoParaAgenda(@org.springframework.data.repository.query.Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> buscarParaAnonimizar(Long id);
}
