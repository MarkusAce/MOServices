package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.agenda.ObservacionVisitaResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.*;
import cl.rollerapp.backend.model.enums.*;
import cl.rollerapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class ObservacionVisitaService {
    private final AgendaBloqueRepository agenda;
    private final ObservacionVisitaRepository observaciones;
    private final UsuarioRepository usuarios;

    @Transactional(readOnly = true)
    public Page<ObservacionVisitaResponse> listar(Long id, Long actorId, boolean administrador, int pagina) {
        AgendaBloque bloque = agenda.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Visita no encontrada."));
        validarAcceso(bloque, actorId, administrador);
        return observaciones.findByBloque_IdOrderByCreadoEnDescIdDesc(id, PageRequest.of(Math.max(0, pagina), 20))
                .map(ObservacionVisitaResponse::desde);
    }

    @Transactional
    public ObservacionVisitaResponse agregar(Long id, Long actorId, boolean administrador, String texto) {
        
        AgendaBloque bloque = agenda.buscarConBloqueoParaActualizar(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Visita no encontrada."));
        validarAcceso(bloque, actorId, administrador);
        if (bloque.getEstado() != EstadoAgenda.RESERVADO && bloque.getEstado() != EstadoAgenda.COMPLETADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se registran observaciones en visitas reservadas o completadas.");
        }
        if (texto == null || texto.isBlank() || texto.length() > 2000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe una observación de hasta 2000 caracteres.");
        }
        Usuario autor = usuarios.findById(actorId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Cuenta no disponible."));
        if (!Boolean.TRUE.equals(autor.getActivo())
                || (administrador ? autor.getRol() != Rol.ADMIN : autor.getRol() != Rol.TECNICO)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tu cuenta no puede registrar observaciones técnicas.");
        }
        ObservacionVisita nota = ObservacionVisita.builder().bloque(bloque).autor(autor)
                .texto(texto.strip()).creadoEn(Instant.now()).build();
        return ObservacionVisitaResponse.desde(observaciones.save(nota));
    }

    private void validarAcceso(AgendaBloque bloque, Long actorId, boolean administrador) {
        if (actorId == null || (!administrador && (bloque.getTecnico() == null
                || !actorId.equals(bloque.getTecnico().getId())))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el técnico asignado o administración puede consultar esta visita.");
        }
    }
}
