package cl.rollerapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "observaciones_visita")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ObservacionVisita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloque_id", nullable = false, updatable = false)
    private AgendaBloque bloque;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false, updatable = false)
    private Usuario autor;
    @Column(nullable = false, updatable = false, length = 2000)
    private String texto;
    @Column(name = "creado_en", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant creadoEn;
    @PrePersist
    public void preparar() { if (creadoEn == null) creadoEn = Instant.now(); }
}
