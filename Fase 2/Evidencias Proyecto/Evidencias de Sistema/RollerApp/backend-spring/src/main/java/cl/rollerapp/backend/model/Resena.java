package cl.rollerapp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Integer calidad;

    @Column(nullable = false)
    private Integer servicio;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean editada;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;

    @PrePersist
    public void prePersist() {
        if (editada == null) editada = false;
        if (fecha == null) fecha = LocalDateTime.now();
    }
}
