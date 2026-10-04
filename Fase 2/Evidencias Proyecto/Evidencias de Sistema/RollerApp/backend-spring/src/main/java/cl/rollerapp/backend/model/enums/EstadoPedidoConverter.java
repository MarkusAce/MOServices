package cl.rollerapp.backend.model.enums;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPedidoConverter extends EnumMinusculaConverter<EstadoPedido> {
    public EstadoPedidoConverter() {
        super(EstadoPedido.class);
    }
}
