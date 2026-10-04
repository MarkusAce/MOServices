package cl.rollerapp.backend.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;


@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionServicioId implements Serializable {

    private Long cotizacion;
    private Long servicio;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CotizacionServicioId that)) return false;
        return Objects.equals(cotizacion, that.cotizacion) && Objects.equals(servicio, that.servicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cotizacion, servicio);
    }
}
