package cl.rollerapp.backend.dto.auth;

public record EnviarCodigoResponse(boolean enviado, String codigoDemo, String correo) {
}
