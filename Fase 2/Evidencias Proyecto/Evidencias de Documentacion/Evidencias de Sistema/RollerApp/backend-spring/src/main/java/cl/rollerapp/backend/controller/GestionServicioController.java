package cl.rollerapp.backend.controller;
import cl.rollerapp.backend.dto.producto.*;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.GestionServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/api/admin/servicios")
@PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class GestionServicioController {
 private final GestionServicioService servicio;
 @GetMapping public List<ServicioResponse> listar() {return servicio.listar();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public ServicioResponse crear(@Valid @RequestBody GuardarServicioRequest r,@AuthenticationPrincipal UsuarioPrincipal p) {
  return servicio.guardar(null,r,p.getId());
 }
 @PutMapping("/{id}")
 public ServicioResponse editar(@PathVariable Long id,@Valid @RequestBody GuardarServicioRequest r,@AuthenticationPrincipal UsuarioPrincipal p) {
  return servicio.guardar(id,r,p.getId());
 }
 @PutMapping("/{id}/disponibilidad")
 public ServicioResponse disponibilidad(@PathVariable Long id,@Valid @RequestBody DisponibilidadServicioRequest r,@AuthenticationPrincipal UsuarioPrincipal p) {
  return servicio.disponibilidad(id,r.activo(),p.getId());
 }
}
