package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.service.DireccionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/direcciones") @RequiredArgsConstructor
public class DireccionesController {
    private final DireccionesService direcciones;
    @GetMapping("/configuracion")
    public Map<String, Boolean> configuracion() { return Map.of("habilitado", direcciones.configurado()); }
    @GetMapping("/sugerencias")
    public List<DireccionesService.Sugerencia> sugerencias(@RequestParam String texto, @RequestParam String token) {
        return direcciones.sugerir(texto, token);
    }
    @GetMapping("/detalle/{placeId}")
    public DireccionesService.DireccionVerificada detalle(@PathVariable String placeId, @RequestParam String token) {
        return direcciones.verificar(placeId, token);
    }
}
