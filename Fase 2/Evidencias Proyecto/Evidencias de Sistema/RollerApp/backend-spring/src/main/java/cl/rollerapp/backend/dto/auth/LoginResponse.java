package cl.rollerapp.backend.dto.auth;

public record LoginResponse(String token, UsuarioResponse usuario) {
}
