package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoriaConverter extends EnumMinusculaConverter<Categoria> {
    public CategoriaConverter() {
        super(Categoria.class);
    }
}
