package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.model.AuditoriaAdministrativa;
import cl.rollerapp.backend.service.AuditoriaAdministrativaService;
import lombok.RequiredArgsConstructor;
import cl.rollerapp.backend.dto.PaginaResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/admin/auditoria")
@PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class AuditoriaAdministrativaController {
    private final AuditoriaAdministrativaService service;
    @GetMapping
    public PaginaResponse<AuditoriaAdministrativa> listar(@RequestParam(defaultValue="0") int pagina,
                                                 @RequestParam(defaultValue="20") int cantidad) {
        return PaginaResponse.desde(service.listar(pagina, cantidad));
    }
}
