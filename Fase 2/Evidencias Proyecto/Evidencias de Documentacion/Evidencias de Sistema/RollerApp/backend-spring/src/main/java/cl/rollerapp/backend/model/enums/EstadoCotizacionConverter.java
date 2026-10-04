package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoCotizacionConverter extends EnumMinusculaConverter<EstadoCotizacion> {
    public EstadoCotizacionConverter() {
        super(EstadoCotizacion.class);
    }
}
