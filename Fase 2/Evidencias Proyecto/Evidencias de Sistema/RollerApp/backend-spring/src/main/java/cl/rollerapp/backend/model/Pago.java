package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.EstadoTransaccion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "pago_cotizaciones", joinColumns = @JoinColumn(name = "pago_id"),
            inverseJoinColumns = @JoinColumn(name = "cotizacion_id"))
    @Builder.Default
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private java.util.List<Cotizacion> cotizacionesAdicionales = new java.util.ArrayList<>();

    
    @Transient
    public java.util.List<Cotizacion> cotizacionesCompra() {
        java.util.List<Cotizacion> todas = new java.util.ArrayList<>();
        todas.add(cotizacion);
        if (cotizacionesAdicionales != null) todas.addAll(cotizacionesAdicionales);
        return todas;
    }

    @Column(name = "token_webpay", length = 255)
    private String tokenWebpay;

    @Column(name = "orden_compra", nullable = false, unique = true, length = 64)
    private String ordenCompra;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "estado_transaccion", nullable = false, length = 20)
    private EstadoTransaccion estadoTransaccion;

    @Column(name = "codigo_autorizacion", length = 20)
    private String codigoAutorizacion;

    @Column(name = "tipo_pago", length = 50)
    private String tipoPago;

    @Column(name = "tarjeta_ultimos_digitos", length = 4)
    private String tarjetaUltimosDigitos;

    @Column(name = "fecha_transaccion")
    private LocalDateTime fechaTransaccion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (estadoTransaccion == null) estadoTransaccion = EstadoTransaccion.INICIADA;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}
