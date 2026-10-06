package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.agenda.AgendaBloqueResponse;
import cl.rollerapp.backend.dto.agenda.CrearBloqueRequest;
import cl.rollerapp.backend.dto.agenda.AgendaAdminResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.AgendaBloque;
import cl.rollerapp.backend.model.Cotizacion;
import cl.rollerapp.backend.model.enums.EstadoAgenda;
import cl.rollerapp.backend.repository.AgendaBloqueRepository;
import cl.rollerapp.backend.repository.CotizacionRepository;
import cl.rollerapp.backend.repository.UsuarioRepository;
import cl.rollerapp.backend.model.Usuario;
import cl.rollerapp.backend.model.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.annotation.Isolation;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendaService {

    private final AgendaBloqueRepository agendaBloqueRepository;
    private final CotizacionRepository cotizacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CoberturaService coberturaService;
    private final CotizacionVigenciaService vigenciaService;

    @Transactional(readOnly = true)
    public List<AgendaAdminResponse> listarParaAdmin() {
        return agendaBloqueRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(LocalDate.now(java.time.ZoneId.of("America/Santiago")))
                .stream().map(AgendaAdminResponse::desde).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AgendaBloqueResponse crearBloque(CrearBloqueRequest req) {
        Usuario tecnico = buscarTecnico(req.tecnicoId());
        validarHorario(req.fecha(), req.horaInicio(), req.horaFin());
        validarMargen(tecnico.getId(), req.fecha(), req.horaInicio(), req.horaFin(), null);
        AgendaBloque bloque = AgendaBloque.builder().fecha(req.fecha()).horaInicio(req.horaInicio())
                .horaFin(req.horaFin()).tecnico(tecnico).estado(EstadoAgenda.DISPONIBLE).build();
        return AgendaBloqueResponse.desde(agendaBloqueRepository.save(bloque));
    }

    @Transactional
    public AgendaBloqueResponse cancelarLibre(Long id) {
        AgendaBloque bloque = agendaBloqueRepository.buscarConBloqueoParaActualizar(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Horario no encontrado."));
        if (bloque.getEstado() != EstadoAgenda.DISPONIBLE) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden cancelar horarios disponibles.");
        }
        bloque.setEstado(EstadoAgenda.CANCELADO);
        return AgendaBloqueResponse.desde(agendaBloqueRepository.save(bloque));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AgendaBloqueResponse asignarTecnico(Long id, Long tecnicoId) {
        
        Usuario tecnico = buscarTecnico(tecnicoId);
        AgendaBloque bloque = agendaBloqueRepository.buscarConBloqueoParaActualizar(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Horario no encontrado."));
        if (bloque.getEstado() != EstadoAgenda.DISPONIBLE) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede asignar técnico a horarios disponibles.");
        }
        validarHorario(bloque.getFecha(), bloque.getHoraInicio(), bloque.getHoraFin());
        validarMargen(tecnicoId, bloque.getFecha(), bloque.getHoraInicio(), bloque.getHoraFin(), id);
        bloque.setTecnico(tecnico);
        return AgendaBloqueResponse.desde(agendaBloqueRepository.save(bloque));
    }

    @Transactional
    public AgendaBloqueResponse completarServicio(Long bloqueId, Long actorId, boolean administrador) {
        AgendaBloque bloque = agendaBloqueRepository.buscarConBloqueoParaActualizar(bloqueId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Visita no encontrada."));
        if (bloque.getEstado() != EstadoAgenda.RESERVADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede completar una visita reservada.");
        }
        if (bloque.getTecnico() == null || (!administrador && !bloque.getTecnico().getId().equals(actorId))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el técnico asignado o el administrador puede completar la visita.");
        }
        if (bloque.getAtencionInicio() == null) throw new ApiException(HttpStatus.CONFLICT, "Registra el inicio de atención antes de completar la visita.");
        if (bloque.getAtencionInicio().isAfter(LocalDateTime.now())) throw new ApiException(HttpStatus.CONFLICT, "El inicio de atención no puede estar en el futuro.");
        bloque.setAtencionFin(LocalDateTime.now());
        bloque.setEstado(EstadoAgenda.COMPLETADO);
        return AgendaBloqueResponse.desde(agendaBloqueRepository.save(bloque));
    }

    @Transactional
    public AgendaBloqueResponse iniciarAtencion(Long id, Long actorId, boolean administrador) {
        AgendaBloque bloque = agendaBloqueRepository.buscarConBloqueoParaActualizar(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Visita no encontrada."));
        if (bloque.getEstado() != EstadoAgenda.RESERVADO) throw new ApiException(HttpStatus.CONFLICT, "La visita debe estar reservada.");
        if (bloque.getTecnico() == null || (!administrador && !bloque.getTecnico().getId().equals(actorId)))
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el técnico asignado o el administrador puede iniciar la atención.");
        if (bloque.getAtencionInicio() == null) bloque.setAtencionInicio(LocalDateTime.now());
        return AgendaBloqueResponse.desde(agendaBloqueRepository.save(bloque));
    }

    private Usuario buscarTecnico(Long id) {
        Usuario tecnico = usuarioRepository.buscarTecnicoParaAgenda(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Técnico no encontrado."));
        if (tecnico.getRol() != Rol.TECNICO || !Boolean.TRUE.equals(tecnico.getActivo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selecciona un técnico activo.");
        }
        return tecnico;
    }

    private void validarHorario(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La hora de término debe ser posterior al inicio.");
        }
        if (!AnticipacionAgenda.cumple(ahoraAgenda(), LocalDateTime.of(fecha, inicio))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El horario debe tener al menos 48 horas hábiles de anticipación.");
        }
    }

    private void validarMargen(Long tecnicoId, LocalDate fecha, LocalTime inicio, LocalTime fin, Long ignorarId) {
        boolean conflicto = agendaBloqueRepository.findByTecnico_IdAndFechaBetween(tecnicoId, fecha.minusDays(1), fecha.plusDays(1)).stream()
                .filter(b -> b.getEstado() != EstadoAgenda.CANCELADO)
                .filter(b -> ignorarId == null || !b.getId().equals(ignorarId))
                .anyMatch(b -> LocalDateTime.of(fecha, inicio).isBefore(
                                LocalDateTime.of(b.getFecha(), b.getHoraFin()).plusMinutes(30))
                        && LocalDateTime.of(fecha, fin).plusMinutes(30).isAfter(
                                LocalDateTime.of(b.getFecha(), b.getHoraInicio())));
        if (conflicto) {
            throw new ApiException(HttpStatus.CONFLICT, "El técnico necesita 30 minutos libres entre visitas.");
        }
    }

    @Transactional(readOnly = true)
    public List<AgendaBloqueResponse> listarDisponibles() {
        return agendaBloqueRepository
                .findByEstadoAndFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(
                        EstadoAgenda.DISPONIBLE, java.time.LocalDate.now(java.time.ZoneId.of("America/Santiago")))
                .stream()
                .filter(b -> cumpleAnticipacionMinima(b))
                .map(AgendaBloqueResponse::desde)
                .toList();
    }

    @Transactional
    public AgendaBloqueResponse preReservar(Long clienteId, Long bloqueId, Long cotizacionId) {
        
        Cotizacion consulta = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));
        if (!consulta.getCliente().getId().equals(clienteId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta cotización no te pertenece.");
        }
        if (vigenciaService.expirarSiVencida(cotizacionId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La cotización expiró tras 15 días. Recálcúlala con las tarifas vigentes antes de agendar.");
        }
        return preReservarVigente(clienteId, bloqueId, cotizacionId);
    }

    
    @Transactional
    public AgendaBloqueResponse preReservarVigente(Long clienteId, Long bloqueId, Long cotizacionId) {
        Cotizacion cotizacion = cotizacionRepository.buscarConBloqueoParaActualizar(cotizacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));
        if (!cotizacion.getCliente().getId().equals(clienteId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta cotización no te pertenece.");
        }
        if (!cotizacion.requiereVisita()) {
            throw new ApiException(HttpStatus.CONFLICT, "Esta compra no incluye servicios. Puedes pagar sin reservar una visita.");
        }
        coberturaService.validarComuna(cotizacion.getComuna());
        if (cotizacion.getEstado() != cl.rollerapp.backend.model.enums.EstadoCotizacion.PENDIENTE_PAGO) {
            throw new ApiException(HttpStatus.CONFLICT, "La cotización ya no está pendiente de pago.");
        }
        if (agendaBloqueRepository.findByCotizacion_Id(cotizacionId).stream()
                .anyMatch(b -> b.getEstado() == EstadoAgenda.PRE_RESERVADO
                        && b.getReservadoHasta() != null && b.getReservadoHasta().isAfter(LocalDateTime.now()))) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esta cotización ya tiene un horario pre-reservado. Espera a que venza antes de elegir otro.");
        }

        AgendaBloque bloque = agendaBloqueRepository.buscarConBloqueoParaActualizar(bloqueId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bloque de agenda no encontrado."));

        if (bloque.getEstado() != EstadoAgenda.DISPONIBLE) {
            throw new ApiException(HttpStatus.CONFLICT, "Ese horario ya no está disponible, elige otro.");
        }
        if (!cumpleAnticipacionMinima(bloque)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Ese horario ya no cumple el mínimo de 48 horas hábiles de anticipación. Elige uno más adelante.");
        }

        bloque.setEstado(EstadoAgenda.PRE_RESERVADO);
        bloque.setCotizacion(cotizacion);
        bloque.setReservadoHasta(LocalDateTime.now().plusMinutes(15));

        return AgendaBloqueResponse.desde(agendaBloqueRepository.save(bloque));
    }

    @Transactional
    public void confirmarReserva(Long bloqueId) {
        AgendaBloque bloque = agendaBloqueRepository.findById(bloqueId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bloque de agenda no encontrado."));
        bloque.setEstado(EstadoAgenda.RESERVADO);
        bloque.setReservadoHasta(null);
        agendaBloqueRepository.save(bloque);
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void liberarBloquesVencidos() {

        LocalDateTime ahora = LocalDateTime.now();
        
        
        List<AgendaBloque> vencidos = agendaBloqueRepository
                .buscarVencidosParaLiberar(EstadoAgenda.PRE_RESERVADO, ahora);
        for (AgendaBloque bloque : vencidos) {
            
            
            bloque.setEstado(EstadoAgenda.DISPONIBLE);
            bloque.setCotizacion(null);
            bloque.setReservadoHasta(null);
            agendaBloqueRepository.save(bloque);
            log.info("Bloque de agenda {} liberado por vencimiento de pre-reserva.", bloque.getId());
        }
    }

    private boolean cumpleAnticipacionMinima(AgendaBloque bloque) {
        LocalDateTime objetivo = LocalDateTime.of(bloque.getFecha(), bloque.getHoraInicio());
        return AnticipacionAgenda.cumple(ahoraAgenda(), objetivo);
    }

    private LocalDateTime ahoraAgenda() {
        return LocalDateTime.now(java.time.ZoneId.of("America/Santiago"));
    }
}
