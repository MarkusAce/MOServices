package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.agenda.AgendaBloqueResponse;
import cl.rollerapp.backend.dto.agenda.PreReservarBloqueRequest;
import cl.rollerapp.backend.dto.agenda.CrearBloqueRequest;
import cl.rollerapp.backend.dto.agenda.AsignarTecnicoRequest;
import cl.rollerapp.backend.dto.agenda.AgendaAdminResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.AgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;
    private final cl.rollerapp.backend.service.AuditoriaAdministrativaService auditoria;

    @GetMapping("/disponibles")
    public List<AgendaBloqueResponse> listarDisponibles() {
        return agendaService.listarDisponibles();
    }

    @PutMapping("/bloques/{id}/pre-reservar")
    public AgendaBloqueResponse preReservar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody PreReservarBloqueRequest req
    ) {
        return agendaService.preReservar(principal.getId(), id, req.cotizacionId());
    }

    @GetMapping("/admin/bloques")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AgendaAdminResponse> listarTodos() {
        return agendaService.listarParaAdmin();
    }

    @PostMapping("/admin/bloques")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaBloqueResponse crear(@Valid @RequestBody CrearBloqueRequest req) {
        return agendaService.crearBloque(req);
    }

    @PutMapping("/admin/bloques/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaBloqueResponse cancelar(@PathVariable Long id) {
        return agendaService.cancelarLibre(id);
    }

    @PutMapping("/bloques/{id}/iniciar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public AgendaBloqueResponse iniciar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return agendaService.iniciarAtencion(id, principal.getId(), principal.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @PutMapping("/bloques/{id}/completar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public AgendaBloqueResponse completar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return agendaService.completarServicio(id, principal.getId(),
                principal.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @PutMapping("/admin/bloques/{id}/tecnico")
    @PreAuthorize("hasRole('ADMIN')")
    public AgendaBloqueResponse asignar(@PathVariable Long id, @Valid @RequestBody AsignarTecnicoRequest req,
                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        AgendaBloqueResponse respuesta = agendaService.asignarTecnico(id, req.tecnicoId());
        auditoria.registrar(principal.getId(), "ASIGNAR_TECNICO", "AGENDA_BLOQUE", id, "tecnicoId=" + req.tecnicoId());
        return respuesta;
    }
}
