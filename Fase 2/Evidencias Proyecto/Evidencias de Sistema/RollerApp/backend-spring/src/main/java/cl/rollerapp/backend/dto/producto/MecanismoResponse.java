package cl.rollerapp.backend.dto.producto;

import cl.rollerapp.backend.model.Mecanismo;

import java.math.BigDecimal;

public record MecanismoResponse(Long id, String nombre, String descripcion, BigDecimal valorFijo, Boolean activo) {
    public static MecanismoResponse desde(Mecanismo m) {
        return new MecanismoResponse(m.getId(), m.getNombre(), m.getDescripcion(), m.getValorFijo(), m.getActivo());
    }
}
