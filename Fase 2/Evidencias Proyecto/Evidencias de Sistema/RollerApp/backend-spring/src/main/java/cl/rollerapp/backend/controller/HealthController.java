package cl.rollerapp.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController @RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbc;
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> salud() {
        try {
            Integer valor = jdbc.queryForObject("SELECT 1", Integer.class);
            if (Integer.valueOf(1).equals(valor)) return ResponseEntity.ok(Map.of("estado", "OK"));
        } catch (RuntimeException ignorada) {  }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("estado", "NO_DISPONIBLE"));
    }
}
