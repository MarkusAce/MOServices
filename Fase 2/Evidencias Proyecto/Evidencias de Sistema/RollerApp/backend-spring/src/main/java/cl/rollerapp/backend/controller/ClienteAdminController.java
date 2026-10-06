package cl.rollerapp.backend.controller;
import cl.rollerapp.backend.dto.auth.UsuarioResponse;
import cl.rollerapp.backend.dto.cotizacion.CotizacionResponse;
import cl.rollerapp.backend.dto.pedido.PedidoResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.enums.Rol;
import cl.rollerapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController @RequiredArgsConstructor @RequestMapping("/api/admin/clientes")
@PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
public class ClienteAdminController {
 private final UsuarioRepository usuarios; private final CotizacionRepository cotizaciones;
 private final PedidoRepository pedidos; private final AgendaBloqueRepository agenda;
 public record Visita(Long id,String fecha,String horaInicio,String estado,String tecnico,Long cotizacionId){}
 public record Ficha(UsuarioResponse cliente,String telefono,List<String> direcciones,List<CotizacionResponse> cotizaciones,List<PedidoResponse> pedidos,List<Visita> visitas){}
 @GetMapping @Transactional(readOnly=true) public List<UsuarioResponse> listar(){
   return usuarios.findAll().stream().filter(u->u.getRol()==Rol.CLIENTE && u.getAnonimizadoEn()==null).map(UsuarioResponse::desde).toList();}
 @GetMapping("/{id}") @Transactional(readOnly=true) public Ficha obtener(@PathVariable Long id){
   var u=usuarios.findById(id).filter(x->x.getAnonimizadoEn()==null).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Cliente no encontrado."));
   var cs=cotizaciones.findByCliente_IdOrderByIdDesc(id);
   var ps=pedidos.findByCliente_IdOrderByIdDesc(id);
   if(u.getRol()!=Rol.CLIENTE && cs.isEmpty() && ps.isEmpty())throw new ApiException(HttpStatus.NOT_FOUND,"Cliente no encontrado.");
   var vs=agenda.findByCotizacion_Cliente_IdOrderByFechaDescHoraInicioDesc(id).stream().map(a->new Visita(a.getId(),a.getFecha().toString(),a.getHoraInicio().toString(),a.getEstado().name(),a.getTecnico()==null?null:a.getTecnico().getNombre()+" "+a.getTecnico().getApellido(),a.getCotizacion().getId())).toList();
   return new Ficha(UsuarioResponse.desde(u),u.getTelefono(),cs.stream().map(c->c.getDireccion()+", "+c.getComuna()).distinct().toList(),
     cs.stream().map(CotizacionResponse::desde).toList(),ps.stream().map(PedidoResponse::desde).toList(),vs);
 }
}
