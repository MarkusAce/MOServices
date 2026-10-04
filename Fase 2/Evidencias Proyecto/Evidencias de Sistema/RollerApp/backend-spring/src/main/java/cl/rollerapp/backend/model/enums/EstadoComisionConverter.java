package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoComisionConverter extends EnumMinusculaConverter<EstadoComision> {
    public EstadoComisionConverter() { super(EstadoComision.class); }
}
