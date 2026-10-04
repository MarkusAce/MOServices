package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.cotizacion.CotizacionResponse;
import cl.rollerapp.backend.dto.cotizacion.CrearCotizacionRequest;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CotizacionResponse crear(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody CrearCotizacionRequest req
    ) {
        return cotizacionService.crear(principal.getId(), req);
    }

    @GetMapping
    public List<CotizacionResponse> listarPropias(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return cotizacionService.listarPropias(principal.getId());
    }

    @PostMapping("/{id}/renovar")
    @ResponseStatus(HttpStatus.CREATED)
    public CotizacionResponse renovar(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        return cotizacionService.renovar(id, principal.getId());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public cl.rollerapp.backend.dto.PaginaResponse<CotizacionResponse> listarParaAdmin(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int cantidad) {
        return cl.rollerapp.backend.dto.PaginaResponse.desde(cotizacionService.listarParaAdmin(pagina, cantidad));
    }

    @GetMapping("/{id}")
    public CotizacionResponse obtenerPorId(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        boolean esAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return cotizacionService.obtenerPorId(id, principal.getId(), esAdmin);
    }
}
