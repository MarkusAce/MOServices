package cl.rollerapp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cotizacion_servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionServicio {

    @EmbeddedId
    private CotizacionServicioId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cotizacion")
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("servicio")
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;
    
    @Column(name = "precio_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "comision_tecnico_aplicada", nullable = false, precision = 10, scale = 2)
    private BigDecimal comisionTecnicoAplicada;
}
