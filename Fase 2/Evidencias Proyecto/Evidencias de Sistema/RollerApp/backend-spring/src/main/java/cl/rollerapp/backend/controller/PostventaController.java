package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.postventa.CambiarEstadoPostventaRequest;
import cl.rollerapp.backend.dto.postventa.CrearPostventaRequest;
import cl.rollerapp.backend.dto.postventa.PostventaResponse;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.PostventaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postventa")
@RequiredArgsConstructor
public class PostventaController {

    private final PostventaService postventaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostventaResponse crear(
            @AuthenticationPrincipal UsuarioPrincipal principal, 
            @Valid @RequestBody CrearPostventaRequest req
    ) {
        Long usuarioId = principal != null ? principal.getId() : null;
        return postventaService.crear(usuarioId, req);
    }

    @GetMapping("/admin/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PostventaResponse> listarTodas() {
        return postventaService.listarTodas();
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public PostventaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoPostventaRequest req) {
        return postventaService.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        postventaService.eliminar(id);
    }
}
