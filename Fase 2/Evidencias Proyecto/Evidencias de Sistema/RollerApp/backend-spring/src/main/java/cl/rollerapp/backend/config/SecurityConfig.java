package cl.rollerapp.backend.config;

import cl.rollerapp.backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService usuarioDetallesService;

    @Value("${app.frontend-origenes}")
    private String frontendOrigenes;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(configuracionCors()))
            .csrf(csrf -> csrf.disable()) 
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(errores -> errores.authenticationEntryPoint((request, response, error) -> response.sendError(401)))
            .authorizeHttpRequests(rutas -> rutas
                
                .requestMatchers(
                    "/api/auth/registro",
                    "/api/auth/login",
                    "/api/auth/olvide-contrasena",
                    "/api/auth/verificar-codigo-recuperacion",
                    "/api/auth/restablecer-contrasena"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/productos/**",
                    "/api/telas",
                    "/api/mecanismos",
                    "/api/servicios",
                    "/api/resenas/**",
                    "/api/agenda/disponibles"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/postventa").permitAll()
                .requestMatchers("/api/pagos/webpay/retorno", "/api/pagos/webpay/abrir").permitAll() 

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource configuracionCors() {
        CorsConfiguration config = new CorsConfiguration();

        if (frontendOrigenes == null || frontendOrigenes.isBlank()) {
            config.setAllowedOrigins(List.of());
        } else {
            config.setAllowedOrigins(Arrays.stream(frontendOrigenes.split(",")).map(String::trim).filter(origen -> !origen.isBlank()).toList());
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
