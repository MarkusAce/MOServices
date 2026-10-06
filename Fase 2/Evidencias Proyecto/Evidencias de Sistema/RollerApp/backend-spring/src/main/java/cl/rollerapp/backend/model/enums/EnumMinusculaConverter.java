package cl.rollerapp.backend.model.enums;

import jakarta.persistence.AttributeConverter;









public abstract class EnumMinusculaConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Class<E> tipoEnum;

    protected EnumMinusculaConverter(Class<E> tipoEnum) {
        this.tipoEnum = tipoEnum;
    }

    @Override
    public String convertToDatabaseColumn(E atributo) {
        return atributo == null ? null : atributo.name().toLowerCase();
    }

    @Override
    public E convertToEntityAttribute(String columna) {
        return columna == null ? null : Enum.valueOf(tipoEnum, columna.toUpperCase());
    }
}
