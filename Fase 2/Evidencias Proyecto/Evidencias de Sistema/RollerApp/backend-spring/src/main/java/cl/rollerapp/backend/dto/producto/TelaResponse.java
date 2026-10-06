package cl.rollerapp.backend.dto.producto;

import cl.rollerapp.backend.model.Tela;
import cl.rollerapp.backend.model.PasoLuz;

import java.math.BigDecimal;

public record TelaResponse(Long id, String nombre, String descripcion, BigDecimal precioM2, Boolean activo, PasoLuz pasoLuz) {
    public static TelaResponse desde(Tela t) {
        return new TelaResponse(t.getId(), t.getNombre(), t.getDescripcion(), t.getPrecioM2(), t.getActivo(), t.getPasoLuz());
    }
}
