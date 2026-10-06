package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolConverter extends EnumMinusculaConverter<Rol> {
    public RolConverter() {
        super(Rol.class);
    }
}
