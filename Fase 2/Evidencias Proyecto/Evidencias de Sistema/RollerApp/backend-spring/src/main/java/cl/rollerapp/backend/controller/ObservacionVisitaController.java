package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.agenda.*;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.ObservacionVisitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/agenda/bloques/{id}/observaciones")
@PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
public class ObservacionVisitaController {
    private final ObservacionVisitaService service;

    @GetMapping
    public PaginaObservacionesVisitaResponse listar(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return PaginaObservacionesVisitaResponse.desde(service.listar(id, principal.getId(), esAdmin(principal), pagina));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ObservacionVisitaResponse agregar(@PathVariable Long id,
            @Valid @RequestBody CrearObservacionVisitaRequest req, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return service.agregar(id, principal.getId(), esAdmin(principal), req.texto());
    }

    private boolean esAdmin(UsuarioPrincipal p) {
        return p.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
