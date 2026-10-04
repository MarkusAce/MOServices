package cl.rollerapp.backend.service;
import cl.rollerapp.backend.dto.pedido.PedidoResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.*;
import cl.rollerapp.backend.model.enums.*;
import cl.rollerapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor
public class VisitaPedidoService {
 private final PedidoRepository pedidos;
 private final AgendaBloqueRepository agenda;
 private final ApplicationEventPublisher eventos;
 @Transactional
 public PedidoResponse cambiar(Long id, Long nuevoId, boolean cancelar) {
   Pedido p=pedidos.buscarParaActualizar(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Pedido no encontrado."));
   if(p.getEstado()==EstadoPedido.REALIZADO || p.getEstado()==EstadoPedido.CANCELADO)
     throw new ApiException(HttpStatus.CONFLICT,"No se puede modificar un pedido cerrado.");
   if(!p.getCotizacion().requiereVisita()) throw new ApiException(HttpStatus.CONFLICT,"Este pedido no requiere visita.");
   AgendaBloque anterior=p.getAgendaBloque();
   if(anterior!=null && anterior.getEstado()==EstadoAgenda.COMPLETADO)
     throw new ApiException(HttpStatus.CONFLICT,"La visita ya se completó.");
   if(cancelar && (anterior==null || anterior.getEstado()!=EstadoAgenda.RESERVADO))
     throw new ApiException(HttpStatus.CONFLICT,"No hay una visita reservada para cancelar.");
   if(!cancelar && nuevoId==null) throw new ApiException(HttpStatus.BAD_REQUEST,"Selecciona el nuevo horario.");
   if(!cancelar && anterior!=null && anterior.getId().equals(nuevoId)) return PedidoResponse.desde(p);
   
   List<Long> ids=new ArrayList<>(); if(anterior!=null)ids.add(anterior.getId()); if(!cancelar)ids.add(nuevoId);
   Map<Long,AgendaBloque> bloques=new HashMap<>();
   ids.stream().distinct().sorted().forEach(bid->bloques.put(bid,agenda.buscarConBloqueoParaActualizar(bid)
       .orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Horario no encontrado."))));
   if(anterior!=null) {
     anterior=bloques.get(anterior.getId());
     if(anterior.getEstado()==EstadoAgenda.COMPLETADO)throw new ApiException(HttpStatus.CONFLICT,"La visita ya se completó.");
   }
   AgendaBloque destino=cancelar?null:bloques.get(nuevoId);
   if(destino!=null) {
     if(destino.getEstado()!=EstadoAgenda.DISPONIBLE || destino.getCotizacion()!=null || pedidos.existsByAgendaBloque_Id(nuevoId))
       throw new ApiException(HttpStatus.CONFLICT,"Ese horario ya está ocupado.");
     if(destino.getTecnico()==null || !Boolean.TRUE.equals(destino.getTecnico().getActivo()) || destino.getTecnico().getRol()!=Rol.TECNICO)
       throw new ApiException(HttpStatus.CONFLICT,"El horario no tiene un técnico activo.");
     if(!AnticipacionAgenda.cumple(LocalDateTime.now(ZoneId.of("America/Santiago")),LocalDateTime.of(destino.getFecha(),destino.getHoraInicio())))
       throw new ApiException(HttpStatus.CONFLICT,"Se requieren 48 horas hábiles de anticipación.");
     destino.setEstado(EstadoAgenda.RESERVADO);destino.setCotizacion(p.getCotizacion());destino.setReservadoHasta(null);agenda.save(destino);
   }
   if(anterior!=null) {anterior.setEstado(EstadoAgenda.CANCELADO);anterior.setReservadoHasta(null);agenda.save(anterior);}
   
   p.setAgendaBloque(destino); if(p.getEstado()==EstadoPedido.EN_TERRENO)p.setEstado(EstadoPedido.REPROGRAMADO);
   p.setVisto(false);pedidos.save(p);
   eventos.publishEvent(new NotificacionTransaccional(p.getCliente().getCorreo(),
     (cancelar?"Visita cancelada":"Visita reagendada")+": pedido #"+p.getId(),
     cancelar?"Se canceló tu visita. Tu pedido conserva su pago y queda pendiente de una nueva fecha. Esta acción no realiza un reembolso.":
       "Nueva visita: "+destino.getFecha()+" de "+destino.getHoraInicio()+" a "+destino.getHoraFin()+". Tu pago se conserva."));
   return PedidoResponse.desde(p);
 }
 @Transactional
 public PedidoResponse verificarDireccion(Long id) {
   Pedido p=pedidos.buscarParaActualizar(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Pedido no encontrado."));
   if(p.getEstado()==EstadoPedido.CANCELADO || p.getEstado()==EstadoPedido.REALIZADO)
     throw new ApiException(HttpStatus.CONFLICT,"El pedido está cerrado.");
   p.getCotizacion().setDireccionPendienteVerificacion(false);
   return PedidoResponse.desde(p);
 }
}
