package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.postventa.CrearPostventaRequest;
import cl.rollerapp.backend.dto.postventa.PostventaResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.PostventaSolicitud;
import cl.rollerapp.backend.model.enums.EstadoPostventa;
import cl.rollerapp.backend.repository.PostventaSolicitudRepository;
import cl.rollerapp.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostventaService {

    private final PostventaSolicitudRepository postventaSolicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PostventaResponse crear(Long usuarioIdOpcional, CrearPostventaRequest req) {
        PostventaSolicitud solicitud = PostventaSolicitud.builder()
                .usuario(usuarioIdOpcional != null ? usuarioRepository.findById(usuarioIdOpcional).orElse(null) : null)
                .nombre(req.nombre())
                .apellido(req.apellido())
                .correo(req.correo())
                .telefono(req.telefono())
                .comuna(req.comuna())
                .descripcion(req.descripcion())
                .estado(EstadoPostventa.PENDIENTE)
                .build();

        return PostventaResponse.desde(postventaSolicitudRepository.save(solicitud));
    }
    
    @Transactional(readOnly = true)
    public List<PostventaResponse> listarTodas() {
        return postventaSolicitudRepository.findAllByOrderByFechaDesc().stream()
                .map(PostventaResponse::desde)
                .toList();
    }

    @Transactional
    public PostventaResponse cambiarEstado(Long id, EstadoPostventa estado) {
        PostventaSolicitud solicitud = postventaSolicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));
        solicitud.setEstado(estado);
        return PostventaResponse.desde(postventaSolicitudRepository.save(solicitud));
    }

    @Transactional
    public void eliminar(Long id) {
        PostventaSolicitud solicitud = postventaSolicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));
        postventaSolicitudRepository.delete(solicitud);
    }
}
