package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.resena.ActualizarResenaRequest;
import cl.rollerapp.backend.dto.resena.CrearResenaRequest;
import cl.rollerapp.backend.dto.resena.ResenaResponse;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.ResenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;

    @GetMapping
    public List<ResenaResponse> listarTodas() {
        return resenaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResenaResponse obtenerPorId(@PathVariable Long id) {
        return resenaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaResponse crear(@AuthenticationPrincipal UsuarioPrincipal principal, @Valid @RequestBody CrearResenaRequest req) {
        return resenaService.crear(principal.getId(), req);
    }

    @PutMapping("/{id}")
    public ResenaResponse actualizar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarResenaRequest req
    ) {
        return resenaService.actualizar(principal.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        resenaService.eliminar(id);
    }
}
