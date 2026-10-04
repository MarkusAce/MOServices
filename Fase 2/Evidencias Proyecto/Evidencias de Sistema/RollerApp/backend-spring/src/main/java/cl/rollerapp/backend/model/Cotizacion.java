package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tela_id", nullable = false)
    private Tela tela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanismo_id", nullable = false)
    private Mecanismo mecanismo;

    @Column(name = "ancho_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal anchoCm;

    @Column(name = "alto_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal altoCm;

    @Column(name = "metros_cuadrados", insertable = false, updatable = false, precision = 8, scale = 4)
    private BigDecimal metrosCuadrados;

    @Column(name = "valor_tela", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTela;

    @Column(name = "valor_mecanismo", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMecanismo;

    @Column(name = "valor_servicios", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorServicios;

    @Column(insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String comuna;

    @Builder.Default
    @Column(name = "direccion_pendiente_verificacion", nullable = false)
    private Boolean direccionPendienteVerificacion = false;

    @Column(name = "referencias_direccion", length = 500)
    private String referenciasDireccion;

    @Column(name = "place_id", length = 255)
    private String placeId;

    @Column(nullable = false, length = 20)
    private EstadoCotizacion estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Builder.Default
    @OneToMany(mappedBy = "cotizacion", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private java.util.List<CotizacionServicio> servicios = new java.util.ArrayList<>();

    
    public boolean requiereVisita() {
        return servicios != null && !servicios.isEmpty();
    }

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoCotizacion.PENDIENTE_PAGO;
        if (valorServicios == null) valorServicios = BigDecimal.ZERO;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}
