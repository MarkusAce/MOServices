package cl.rollerapp.backend.service;

import cl.rollerapp.backend.model.enums.EstadoPedido;
import cl.rollerapp.backend.model.enums.EstadoPostventa;
import cl.rollerapp.backend.model.enums.Rol;
import cl.rollerapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RetencionCuentasService {
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final CotizacionRepository cotizaciones;
    private final PostventaSolicitudRepository postventa;
    private final ResenaRepository resenas;
    private final ObservacionVisitaRepository observaciones;

    @Transactional
    public boolean anonimizarSiCorresponde(Long id, LocalDateTime limite) {
        var usuario = usuarios.buscarParaAnonimizar(id).orElse(null);
        if (usuario == null || usuario.getRol() != Rol.CLIENTE
                || usuario.getAnonimizadoEn() != null
                || usuario.getUltimoAccesoEn() == null
                || !usuario.getUltimoAccesoEn().isBefore(limite)) return false;

        var ordenes = pedidos.findByCliente_IdOrderByIdDesc(id);
        if (ordenes.stream().anyMatch(p -> p.getEstado() != EstadoPedido.REALIZADO
                && p.getEstado() != EstadoPedido.CANCELADO)) return false;
        if (ordenes.stream().anyMatch(p -> p.getCreadoEn() != null && !p.getCreadoEn().isBefore(limite))) return false;

        var presupuestos = cotizaciones.findByCliente_IdOrderByIdDesc(id);
        if (presupuestos.stream().anyMatch(c -> c.getCreadoEn() != null && !c.getCreadoEn().isBefore(limite))) return false;
        var solicitudes = postventa.findByUsuario_Id(id);
        if (solicitudes.stream().anyMatch(s -> s.getEstado() == EstadoPostventa.PENDIENTE)) return false;

        observaciones.anonimizarPorCliente(id);

        
        presupuestos.forEach(c -> {
            c.setDireccion("[dato eliminado]");
            c.setComuna("[dato eliminado]");
        });
        cotizaciones.saveAll(presupuestos);
        solicitudes.forEach(s -> {
            s.setNombre("Cliente"); s.setApellido("Anonimizado");
            s.setCorreo("anonimo-" + s.getId() + "@invalid.local");
            s.setTelefono("[eliminado]"); s.setComuna("[eliminado]");
            s.setDescripcion("[contenido eliminado]");
        });
        postventa.saveAll(solicitudes);
        var opiniones = resenas.findByUsuario_Id(id);
        opiniones.forEach(r -> {
            r.setNombre("Cliente anónimo");
            r.setDescripcion("[contenido eliminado]");
        });
        resenas.saveAll(opiniones);

        usuario.setNombre("Cliente"); usuario.setApellido("Anonimizado");
        usuario.setCorreo("anonimo-" + id + "@invalid.local");
        usuario.setTelefono(null);
        usuario.setContrasenaHash("cuenta-inhabilitada-" + UUID.randomUUID());
        usuario.setCodigoVerificacion(null); usuario.setCodigoExpira(null);
        usuario.setCorreoPendiente(null); usuario.setCodigoCambioCorreo(null);
        usuario.setCodigoCambioCorreoExpira(null); usuario.setCodigoRecuperacion(null);
        usuario.setCodigoRecuperacionExpira(null); usuario.setResetNonce(null);
        usuario.setCambioContrasenaAutorizadoHasta(null);
        usuario.setActivo(false); usuario.setAnonimizadoEn(LocalDateTime.now());
        usuarios.save(usuario);
        return true;
    }
}
