package cl.rollerapp.backend.service;
import cl.rollerapp.backend.dto.producto.*;
import cl.rollerapp.backend.model.*;
import cl.rollerapp.backend.repository.*;
import cl.rollerapp.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import java.util.List;
@Service @RequiredArgsConstructor
public class GestionServicioService {
 private final ServicioRepository servicios;
 private final AuditoriaAdministrativaRepository auditoria;
 @Transactional(readOnly=true)
 public List<ServicioResponse> listar() {
  return servicios.findAll(Sort.by("nombre", "id")).stream().map(ServicioResponse::desde).toList();
 }
 @Transactional
 public ServicioResponse guardar(Long id, GuardarServicioRequest r, Long actor) {
  Servicio s=id==null?new Servicio():obtener(id);
  s.setNombre(r.nombre().trim());
  s.setDescripcion(r.descripcion()==null||r.descripcion().isBlank()?null:r.descripcion().trim());
  s.setPrecio(r.precio());s.setComisionTecnico(r.comisionTecnico());s.setActivo(r.activo());
  s=servicios.save(s);
  registrar(actor,id==null?"CREAR_SERVICIO":"EDITAR_SERVICIO",s);
  return ServicioResponse.desde(s);
 }
 @Transactional
 public ServicioResponse disponibilidad(Long id, Boolean activo, Long actor) {
  Servicio s=obtener(id);s.setActivo(activo);s=servicios.save(s);
  registrar(actor,activo?"ACTIVAR_SERVICIO":"DESACTIVAR_SERVICIO",s);
  return ServicioResponse.desde(s);
 }
 private Servicio obtener(Long id) {
  return servicios.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Servicio no encontrado"));
 }
 private void registrar(Long actor,String accion,Servicio s) {
  AuditoriaAdministrativa a=new AuditoriaAdministrativa();a.actorId=actor;a.accion=accion;
  a.recurso="SERVICIO";a.recursoId=s.getId();
  a.detalle="Precio="+s.getPrecio()+"; comisión="+s.getComisionTecnico()+"; activo="+s.getActivo();
  auditoria.save(a);
 }
}
