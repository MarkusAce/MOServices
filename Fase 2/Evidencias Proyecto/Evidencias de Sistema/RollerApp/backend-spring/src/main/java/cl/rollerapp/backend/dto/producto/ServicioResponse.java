package cl.rollerapp.backend.dto.producto;

import cl.rollerapp.backend.model.Servicio;

import java.math.BigDecimal;

public record ServicioResponse(Long id, String nombre, String descripcion, BigDecimal precio, Boolean activo, BigDecimal comisionTecnico) {
    public static ServicioResponse desde(Servicio s) {
        return new ServicioResponse(s.getId(), s.getNombre(), s.getDescripcion(), s.getPrecio(), s.getActivo(), s.getComisionTecnico());
    }
}
