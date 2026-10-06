package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.resena.ActualizarResenaRequest;
import cl.rollerapp.backend.dto.resena.CrearResenaRequest;
import cl.rollerapp.backend.dto.resena.ResenaResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.Resena;
import cl.rollerapp.backend.model.Usuario;
import cl.rollerapp.backend.repository.ResenaRepository;
import cl.rollerapp.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ResenaResponse> listarTodas() {
        return resenaRepository.findAllByOrderByFechaDesc().stream().map(ResenaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ResenaResponse obtenerPorId(Long id) {
        return ResenaResponse.desde(buscarOFallar(id));
    }

    @Transactional
    public ResenaResponse crear(Long usuarioId, CrearResenaRequest req) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        Resena resena = Resena.builder()
                .usuario(usuario)
                .nombre(usuario.getNombre() + " " + usuario.getApellido())
                .descripcion(req.descripcion())
                .calidad(req.calidad())
                .servicio(req.servicio())
                .editada(false)
                .build();

        return ResenaResponse.desde(resenaRepository.save(resena));
    }

    @Transactional
    public ResenaResponse actualizar(Long usuarioId, Long resenaId, ActualizarResenaRequest req) {
        Resena resena = buscarOFallar(resenaId);

        if (resena.getUsuario() == null || !resena.getUsuario().getId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo puedes modificar tu propia reseña.");
        }

        if (req.descripcion() != null) resena.setDescripcion(req.descripcion());
        if (req.calidad() != null) resena.setCalidad(req.calidad());
        if (req.servicio() != null) resena.setServicio(req.servicio());
        resena.setEditada(true);
        resena.setFechaEdicion(LocalDateTime.now());

        return ResenaResponse.desde(resenaRepository.save(resena));
    }

    @Transactional
    public void eliminar(Long resenaId) {
        Resena resena = buscarOFallar(resenaId);
        resenaRepository.delete(resena);
    }

    private Resena buscarOFallar(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reseña no encontrada."));
    }
}
