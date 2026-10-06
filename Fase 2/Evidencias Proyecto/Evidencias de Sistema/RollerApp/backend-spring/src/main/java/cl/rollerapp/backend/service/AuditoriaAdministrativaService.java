package cl.rollerapp.backend.service;

import cl.rollerapp.backend.model.AuditoriaAdministrativa;
import cl.rollerapp.backend.repository.AuditoriaAdministrativaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuditoriaAdministrativaService {
    private final AuditoriaAdministrativaRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long actorId, String accion, String recurso, Long recursoId, String detalle) {
        AuditoriaAdministrativa evento = new AuditoriaAdministrativa();
        evento.actorId = actorId;
        evento.accion = accion;
        evento.recurso = recurso;
        evento.recursoId = recursoId;
        evento.detalle = detalle;
        repository.save(evento);
    }

    @Transactional(readOnly=true)
    public Page<AuditoriaAdministrativa> listar(int pagina, int cantidad) {
        return repository.findAllByOrderByCreadoEnDescIdDesc(PageRequest.of(Math.max(0,pagina), Math.max(1,Math.min(50,cantidad))));
    }
}
