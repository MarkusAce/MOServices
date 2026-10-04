package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.EstadoAgenda;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "agenda_bloques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendaBloque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @Column(nullable = false, length = 20)
    private EstadoAgenda estado;

    @Column(name = "reservado_hasta")
    private LocalDateTime reservadoHasta;

    @Column(name = "atencion_inicio")
    private LocalDateTime atencionInicio;

    @Column(name = "atencion_fin")
    private LocalDateTime atencionFin;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoAgenda.DISPONIBLE;
    }
}
