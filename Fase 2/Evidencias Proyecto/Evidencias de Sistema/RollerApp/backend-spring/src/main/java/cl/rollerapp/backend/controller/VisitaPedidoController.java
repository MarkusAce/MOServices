package cl.rollerapp.backend.controller;
import cl.rollerapp.backend.dto.pedido.PedidoResponse;
import cl.rollerapp.backend.dto.pago.ReagendarPedidoRequest;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor @RequestMapping("/api/admin/pedidos") @PreAuthorize("hasRole('ADMIN')")
public class VisitaPedidoController {
 private final VisitaPedidoService service; private final AuditoriaAdministrativaService auditoria;
 @PutMapping("/{id}/visita") public PedidoResponse reagendar(@PathVariable Long id,@Valid @RequestBody ReagendarPedidoRequest req,@AuthenticationPrincipal UsuarioPrincipal actor){
   PedidoResponse r=service.cambiar(id,req.bloqueId(),false);auditoria.registrar(actor.getId(),"REAGENDAR_VISITA","PEDIDO",id,"bloque="+req.bloqueId());return r;}
 @DeleteMapping("/{id}/visita") public PedidoResponse cancelar(@PathVariable Long id,@AuthenticationPrincipal UsuarioPrincipal actor){
   PedidoResponse r=service.cambiar(id,null,true);auditoria.registrar(actor.getId(),"CANCELAR_VISITA","PEDIDO",id,"Pago conservado");return r;}
 @PutMapping("/{id}/direccion/verificar") public PedidoResponse verificar(@PathVariable Long id,@AuthenticationPrincipal UsuarioPrincipal actor){
   PedidoResponse r=service.verificarDireccion(id);auditoria.registrar(actor.getId(),"VERIFICAR_DIRECCION","PEDIDO",id,"Verificación manual");return r;}
}
