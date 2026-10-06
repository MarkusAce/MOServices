package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.EstadoComision;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comisiones", uniqueConstraints = @UniqueConstraint(name = "uq_comision_pedido_usuario", columnNames = {"pedido_id", "usuario_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comision {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    
    @Column(precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private EstadoComision estado;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    @Column(name = "pagada_en")
    private LocalDateTime pagadaEn;

    @PrePersist
    void prePersist() {
        if (estado == null) estado = EstadoComision.LIQUIDABLE;
        if (creadaEn == null) creadaEn = LocalDateTime.now();
    }
}
