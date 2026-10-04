package cl.rollerapp.backend.dto.auth;

import cl.rollerapp.backend.model.Usuario;
import cl.rollerapp.backend.model.enums.Rol;

public record UsuarioResponse(Long id, String nombre, String apellido, String correo, Rol rol, Boolean activo) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getRol(), u.getActivo());
    }
}
