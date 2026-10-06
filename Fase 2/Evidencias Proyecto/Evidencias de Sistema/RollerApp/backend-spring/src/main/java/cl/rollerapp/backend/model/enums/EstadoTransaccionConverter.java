package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoTransaccionConverter extends EnumMinusculaConverter<EstadoTransaccion> {
    public EstadoTransaccionConverter() {
        super(EstadoTransaccion.class);
    }
}
