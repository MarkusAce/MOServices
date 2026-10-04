package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPostventaConverter extends EnumMinusculaConverter<EstadoPostventa> {
    public EstadoPostventaConverter() {
        super(EstadoPostventa.class);
    }
}
