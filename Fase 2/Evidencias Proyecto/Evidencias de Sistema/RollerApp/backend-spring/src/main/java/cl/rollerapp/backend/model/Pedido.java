package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id", nullable = false, unique = true)
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_bloque_id")
    private AgendaBloque agendaBloque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(nullable = false)
    private Boolean visto;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoPedido.PAGADO;
        if (visto == null) visto = true;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}
