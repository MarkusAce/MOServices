package cl.rollerapp.backend.security;

import cl.rollerapp.backend.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);

            try {
                Claims claims = jwtService.validarYObtenerClaims(token);

                if (jwtService.esTipoSesion(claims) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Long usuarioId = jwtService.obtenerIdUsuario(claims);
                    Optional<UsuarioPrincipal> principal = usuarioRepository.findById(usuarioId)
                            .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                            .map(UsuarioPrincipal::new);

                    if (principal.isPresent()) {
                        var autenticacion = new UsernamePasswordAuthenticationToken(
                                principal.get(), null, principal.get().getAuthorities());
                        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    }
                }
            } catch (JwtException | IllegalArgumentException excepcion) {

            }
        }

        filterChain.doFilter(request, response);
    }
}
