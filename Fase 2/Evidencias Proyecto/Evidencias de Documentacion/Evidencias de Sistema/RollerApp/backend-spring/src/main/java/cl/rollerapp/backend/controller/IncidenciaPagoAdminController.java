package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.pago.IncidenciaPagoAdminResponse;
import cl.rollerapp.backend.dto.pago.ReagendarPedidoRequest;
import cl.rollerapp.backend.service.IncidenciaPagoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagos/admin/incidencias")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class IncidenciaPagoAdminController {
    private final IncidenciaPagoAdminService service;
    private final cl.rollerapp.backend.service.PagoConciliacionService conciliacion;
    private final cl.rollerapp.backend.service.AuditoriaAdministrativaService auditoria;

    @GetMapping
    public List<IncidenciaPagoAdminResponse> listar() { return service.listar(); }

    @PostMapping("/{pagoId}/revisar")
    public java.util.Map<String, String> revisar(@PathVariable Long pagoId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal cl.rollerapp.backend.security.UsuarioPrincipal principal) {
        String resultado = conciliacion.revisarPago(pagoId);
        auditoria.registrar(principal.getId(), "REVISAR_PAGO_WEBPAY", "PAGO", pagoId, resultado);
        return java.util.Map.of("mensaje", resultado);
    }

    @PutMapping("/pedidos/{pedidoId}/reagendar")
    public ResponseEntity<Void> reagendar(@PathVariable Long pedidoId,
                                          @Valid @RequestBody ReagendarPedidoRequest req,
                                          @org.springframework.security.core.annotation.AuthenticationPrincipal cl.rollerapp.backend.security.UsuarioPrincipal principal) {
        service.reagendar(pedidoId, req.bloqueId());
        auditoria.registrar(principal.getId(), "REAGENDAR_INCIDENCIA", "PEDIDO", pedidoId, "bloqueId=" + req.bloqueId());
        return ResponseEntity.noContent().build();
    }
}
