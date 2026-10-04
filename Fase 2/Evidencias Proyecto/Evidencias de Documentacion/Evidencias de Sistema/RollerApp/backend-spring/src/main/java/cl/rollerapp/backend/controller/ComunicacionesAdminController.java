package cl.rollerapp.backend.controller;
import cl.rollerapp.backend.repository.CorreoPendienteRepository;
import cl.rollerapp.backend.service.ColaCorreosService;
import cl.rollerapp.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.*;
@RestController @RequestMapping("/api/admin/comunicaciones") @PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class ComunicacionesAdminController{
 private final CorreoPendienteRepository correos;private final ColaCorreosService cola;
 @Value("${spring.mail.host:}") private String smtp;
 @Value("${app.correo.empresa:}") private String empresa;
 @Value("${webpay.ambiente}") private String ambiente;
 @Value("${app.backend-url-publica}") private String backend;
 public record ResumenCorreo(Long id,String destinatario,String asunto,String estado,Integer intentos,LocalDateTime creadoEn,LocalDateTime enviadoEn){}
 @GetMapping("/estado") public Map<String,Object> estado(){return Map.of("smtpConfigurado",smtp!=null&&!smtp.isBlank(),"correoEmpresaConfigurado",empresa!=null&&!empresa.isBlank(),"webpayAmbiente",ambiente,"retornoHttps",backend.startsWith("https://"));}
 @GetMapping @Transactional(readOnly=true) public List<ResumenCorreo> listar(){return correos.findTop100ByOrderByCreadoEnDesc().stream().map(c->new ResumenCorreo(c.getId(),c.getDestinatario(),c.getAsunto(),c.getEstado(),c.getIntentos(),c.getCreadoEn(),c.getEnviadoEn())).toList();}
 @PostMapping("/{id}/reintentar") public Map<String,String> reintentar(@PathVariable Long id){cola.reactivar(id);cola.intentar(id);return Map.of("mensaje","Reintento procesado. Revisa el estado del correo.");}
}
