package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoAgendaConverter extends EnumMinusculaConverter<EstadoAgenda> {
    public EstadoAgendaConverter() {
        super(EstadoAgenda.class);
    }
}
