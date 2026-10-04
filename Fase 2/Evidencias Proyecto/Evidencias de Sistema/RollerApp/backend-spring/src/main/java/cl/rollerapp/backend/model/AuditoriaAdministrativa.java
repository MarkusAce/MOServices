package cl.rollerapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_administrativa")
public class AuditoriaAdministrativa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name="actor_id", nullable=false) public Long actorId;
    @Column(name="accion", nullable=false, length=60) public String accion;
    @Column(name="recurso", nullable=false, length=60) public String recurso;
    @Column(name="recurso_id", nullable=false) public Long recursoId;
    @Column(name="detalle", length=250) public String detalle;
    @Column(name="creado_en", nullable=false, updatable=false) public LocalDateTime creadoEn;
    @PrePersist void preparar() { if (creadoEn == null) creadoEn = LocalDateTime.now(); }
}
