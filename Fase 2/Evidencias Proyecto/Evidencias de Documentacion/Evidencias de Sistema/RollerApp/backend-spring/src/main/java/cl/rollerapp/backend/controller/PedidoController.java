package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.pedido.CambiarEstadoPedidoRequest;
import cl.rollerapp.backend.dto.pedido.AsignarVendedorRequest;
import cl.rollerapp.backend.dto.pedido.NotificacionesRespuesta;
import cl.rollerapp.backend.dto.pedido.PedidoResponse;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.model.enums.EstadoPedido;
import cl.rollerapp.backend.model.enums.Rol;
import cl.rollerapp.backend.exception.ApiException;
import org.springframework.http.HttpStatus;
import cl.rollerapp.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final cl.rollerapp.backend.service.AuditoriaAdministrativaService auditoria;

    @GetMapping
    public List<PedidoResponse> listarPropios(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return pedidoService.listarPropios(principal.getId());
    }

    @GetMapping("/notificaciones")
    public List<PedidoResponse> listarNotificaciones(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return pedidoService.listarNotificacionesPendientes(principal.getId());
    }

    @PutMapping("/notificaciones/marcar-vistos")
    public NotificacionesRespuesta marcarNotificacionesVistas(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return new NotificacionesRespuesta(pedidoService.marcarNotificacionesVistas(principal.getId()));
    }

    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public List<PedidoResponse> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/mis-visitas")
    @PreAuthorize("hasRole('TECNICO')")
    public List<PedidoResponse> listarMisVisitas(@AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha) {
        return pedidoService.listarMisVisitas(principal.getId(), fecha);
    }

    @PatchMapping("/{id}/vendedor")
    @PreAuthorize("hasRole('ADMIN')")
    public PedidoResponse asignarVendedor(@PathVariable Long id, @Valid @RequestBody AsignarVendedorRequest req,
                                          @AuthenticationPrincipal UsuarioPrincipal principal) {
        PedidoResponse respuesta = pedidoService.asignarVendedor(id, req.vendedorId());
        auditoria.registrar(principal.getId(), "ASIGNAR_VENDEDOR", "PEDIDO", id, "vendedorId=" + req.vendedorId());
        return respuesta;
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoPedidoRequest req,
                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        boolean administrador = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        PedidoResponse respuesta = pedidoService.cambiarEstado(id, req.estado(), principal.getId(), administrador);
        if (administrador) auditoria.registrar(principal.getId(), "CAMBIAR_ESTADO_PEDIDO", "PEDIDO", id, "estado=" + req.estado());
        return respuesta;
    }
}
