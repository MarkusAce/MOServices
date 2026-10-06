package cl.rollerapp.backend.security;

import cl.rollerapp.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionSesionMs;

    public JwtService(
            @Value("${jwt.secreto}") String secreto,
            @Value("${jwt.expiracion-horas}") long expiracionHoras
    ) {
        this.clave = secreto.isBlank() ? Jwts.SIG.HS256.key().build() : Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionSesionMs = expiracionHoras * 60 * 60 * 1000;
    }

    public String generarTokenSesion(Usuario usuario) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionSesionMs);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("tipo", "sesion")
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clave)
                .compact();
    }

    public String generarTokenRecuperacion(Long usuarioId, String nonce) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + 10 * 60 * 1000);

        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("tipo", "recuperacion-contrasena")
                .claim("nonce", nonce)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clave)
                .compact();
    }

    public Claims validarYObtenerClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long obtenerIdUsuario(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public boolean esTipoSesion(Claims claims) {
        return "sesion".equals(claims.get("tipo", String.class));
    }

    public boolean esTipoRecuperacion(Claims claims) {
        return "recuperacion-contrasena".equals(claims.get("tipo", String.class));
    }
}
