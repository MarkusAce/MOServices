package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.MensajeRespuesta;
import cl.rollerapp.backend.dto.auth.*;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.Usuario;
import cl.rollerapp.backend.model.enums.Rol;
import cl.rollerapp.backend.repository.UsuarioRepository;
import cl.rollerapp.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CorreoService correoService;
    private final ApplicationEventPublisher eventos;
    private final cl.rollerapp.backend.repository.AgendaBloqueRepository agenda;
    private final cl.rollerapp.backend.repository.PedidoRepository pedidos;

    private static final SecureRandom ALEATORIO = new SecureRandom();


    @Transactional
    public MensajeRespuesta registrar(RegistroRequest req) {
        if (usuarioRepository.existsByCorreoIgnoreCase(req.correo())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(req.nombre())
                .apellido(req.apellido())
                .correo(req.correo())
                .contrasenaHash(passwordEncoder.encode(req.contrasena()))
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);
        eventos.publishEvent(new NotificacionTransaccional(
                usuario.getCorreo(), "Bienvenido a RollerApp",
                "Hola " + usuario.getNombre() + ", tu cuenta está creada. Ya puedes cotizar tus cortinas."));
        return new MensajeRespuesta("Cuenta creada correctamente.");
    }

    @Transactional
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(req.correo())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta cuenta ha sido suspendida.");
        }
        if (!passwordEncoder.matches(req.contrasena(), usuario.getContrasenaHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        usuario.setUltimoAccesoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);
        String token = jwtService.generarTokenSesion(usuario);
        return new LoginResponse(token, UsuarioResponse.desde(usuario));
    }


    public UsuarioResponse obtenerPerfil(Long usuarioId) {
        return UsuarioResponse.desde(obtenerUsuarioActivoOFallar(usuarioId));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long usuarioId, ActualizarPerfilRequest req) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);

        if (req.correo() != null && !req.correo().isBlank()
                && !req.correo().equalsIgnoreCase(usuario.getCorreo())) {
            if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(req.correo(), usuarioId)) {
                throw new ApiException(HttpStatus.CONFLICT, "Ese correo ya está en uso por otra cuenta.");
            }
            throw new ApiException(HttpStatus.BAD_REQUEST, "Para cambiar el correo utiliza la verificación de seguridad.");
        }
        if (req.nombre() != null && !req.nombre().isBlank()) usuario.setNombre(req.nombre());
        if (req.apellido() != null && !req.apellido().isBlank()) usuario.setApellido(req.apellido());

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }


    public void verificarContrasenaActual(Long usuarioId, String contrasenaActual) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);
        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasenaHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La contraseña actual no es correcta.");
        }
    }

    @Transactional
    public EnviarCodigoResponse enviarCodigoVerificacion(Long usuarioId, String contrasenaActual) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);
        verificarContrasenaActual(usuarioId, contrasenaActual);
        String codigo = generarCodigoSeisDigitos();
        if (!correoService.enviar(usuario.getCorreo(), "Confirmación de seguridad — RollerApp",
                "Tu código para cambiar la contraseña es " + codigo + ". Vence en 10 minutos. Si no lo solicitaste, ignora este correo.")) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo enviar el correo. Configura SMTP para habilitar cambios de seguridad.");
        }

        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpira(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);


        return new EnviarCodigoResponse(true, null, usuario.getCorreo());
    }

    @Transactional
    public void verificarCodigo(Long usuarioId, String codigo) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpira() == null
                || LocalDateTime.now().isAfter(usuario.getCodigoExpira())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código expiró. Solicita uno nuevo.");
        }
        if (!codigo.equals(usuario.getCodigoVerificacion())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código ingresado no es correcto.");
        }

        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpira(null);
        usuario.setCambioContrasenaAutorizadoHasta(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);
    }


    @Transactional
    public MensajeRespuesta solicitarCambioCorreo(Long usuarioId, String contrasenaActual, String nuevoCorreo) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);
        verificarContrasenaActual(usuarioId, contrasenaActual);
        if (nuevoCorreo == null || !nuevoCorreo.matches("^[^@ ]+@[^@ ]+\\.[^@ ]+$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ingresa un correo válido.");
        }
        nuevoCorreo = nuevoCorreo.trim();
        if (nuevoCorreo.equalsIgnoreCase(usuario.getCorreo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El nuevo correo es igual al actual.");
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(nuevoCorreo, usuarioId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ese correo ya está en uso por otra cuenta.");
        }
        String codigo = generarCodigoSeisDigitos();
        if (!correoService.enviar(nuevoCorreo, "Verifica tu nuevo correo — RollerApp",
                "Tu código para confirmar el nuevo correo es " + codigo + ". Vence en 10 minutos. Si no lo solicitaste, ignora este mensaje.")) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo enviar el correo. Configura SMTP para habilitar cambios de seguridad.");
        }
        usuario.setCorreoPendiente(nuevoCorreo);
        usuario.setCodigoCambioCorreo(codigo);
        usuario.setCodigoCambioCorreoExpira(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);
        return new MensajeRespuesta("Enviamos un código al nuevo correo. Comprueba tu bandeja de entrada.");
    }

    @Transactional
    public UsuarioResponse confirmarCambioCorreo(Long usuarioId, String codigo) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);
        if (usuario.getCorreoPendiente() == null || usuario.getCodigoCambioCorreo() == null
                || usuario.getCodigoCambioCorreoExpira() == null
                || !LocalDateTime.now().isBefore(usuario.getCodigoCambioCorreoExpira())
                || codigo == null || !codigo.equals(usuario.getCodigoCambioCorreo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código no es válido o ha expirado. Solicita uno nuevo.");
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(usuario.getCorreoPendiente(), usuarioId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ese correo ya está en uso por otra cuenta.");
        }
        usuario.setCorreo(usuario.getCorreoPendiente());
        usuario.setCorreoPendiente(null);
        usuario.setCodigoCambioCorreo(null);
        usuario.setCodigoCambioCorreoExpira(null);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public MensajeRespuesta cambiarContrasena(Long usuarioId, String nuevaContrasena) {
        Usuario usuario = obtenerUsuarioActivoOFallar(usuarioId);

        if (usuario.getCambioContrasenaAutorizadoHasta() == null
                || LocalDateTime.now().isAfter(usuario.getCambioContrasenaAutorizadoHasta())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Debes verificar tu contraseña actual y el código antes de continuar.");
        }

        if (nuevaContrasena == null || nuevaContrasena.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 8 caracteres.");
        }
        usuario.setContrasenaHash(passwordEncoder.encode(nuevaContrasena));
        usuario.setCambioContrasenaAutorizadoHasta(null);
        usuarioRepository.save(usuario);

        return new MensajeRespuesta("Contraseña actualizada correctamente.");
    }



    @Transactional
    public OlvideContrasenaResponse olvideContrasena(String correo) {

        String mensajeGenerico = "Si el correo está registrado, te enviamos un código de verificación.";

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            return new OlvideContrasenaResponse(mensajeGenerico, null);
        }

        String codigo = generarCodigoSeisDigitos();
        usuario.setCodigoRecuperacion(codigo);
        usuario.setCodigoRecuperacionExpira(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);

        boolean enviado = correoService.enviar(
                usuario.getCorreo(),
                "Código para recuperar tu contraseña — RollerApp",
                "Tu código de verificación es " + codigo + ". Vence en 10 minutos. Si no fuiste tú, ignora este mensaje."
        );

        if (!enviado) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "La recuperación por correo no está disponible. Configura SMTP.");
        }
        return new OlvideContrasenaResponse(mensajeGenerico, null);
    }

    @Transactional
    public VerificarCodigoRecuperacionResponse verificarCodigoRecuperacion(String correo, String codigo) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);

        if (usuario == null || usuario.getCodigoRecuperacion() == null
                || usuario.getCodigoRecuperacionExpira() == null
                || LocalDateTime.now().isAfter(usuario.getCodigoRecuperacionExpira())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código expiró o no es válido. Solicita uno nuevo.");
        }
        if (!codigo.equals(usuario.getCodigoRecuperacion())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código ingresado no es correcto.");
        }

        String nonce = UUID.randomUUID().toString().replace("-", "");
        usuario.setCodigoRecuperacion(null);
        usuario.setCodigoRecuperacionExpira(null);
        usuario.setResetNonce(nonce);
        usuarioRepository.save(usuario);

        String resetToken = jwtService.generarTokenRecuperacion(usuario.getId(), nonce);
        return new VerificarCodigoRecuperacionResponse(resetToken);
    }

    @Transactional
    public MensajeRespuesta restablecerContrasena(String resetToken, String nuevaContrasena) {
        Claims claims;
        try {
            claims = jwtService.validarYObtenerClaims(resetToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "El enlace de recuperación expiró o no es válido. Solicita uno nuevo.");
        }
        if (!jwtService.esTipoRecuperacion(claims)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido.");
        }

        Long usuarioId = jwtService.obtenerIdUsuario(claims);
        String nonceDelToken = claims.get("nonce", String.class);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        if (usuario.getResetNonce() == null || !usuario.getResetNonce().equals(nonceDelToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Este enlace de recuperación ya fue usado. Solicita uno nuevo.");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(nuevaContrasena));
        usuario.setResetNonce(null);
        usuarioRepository.save(usuario);

        return new MensajeRespuesta("Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
    }


    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional
    public UsuarioResponse cambiarEstadoUsuario(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        if (usuario.getRol() == Rol.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes banear a un administrador.");
        }

        usuario.setActivo(activo);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }


    @Transactional
    public UsuarioResponse crearPersonal(CrearPersonalRequest req) {
        if (req.rol() == Rol.CLIENTE) throw new ApiException(HttpStatus.BAD_REQUEST, "Utiliza el registro de clientes para este rol.");
        String correo = req.correo().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCase(correo))
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        Usuario usuario = Usuario.builder().nombre(req.nombre().trim()).apellido(req.apellido().trim())
                .correo(correo).contrasenaHash(passwordEncoder.encode(req.contrasena()))
                .rol(req.rol()).activo(true).build();
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol rol, Long actorId) {
        Usuario usuario = usuarioRepository.buscarTecnicoParaAgenda(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        if (usuario.getRol() == rol) return UsuarioResponse.desde(usuario);
        if (usuario.getAnonimizadoEn() != null) throw new ApiException(HttpStatus.CONFLICT, "No se puede modificar una cuenta anonimizada.");
        if (id.equals(actorId)) throw new ApiException(HttpStatus.CONFLICT, "No puedes cambiar tu propio rol.");
        if (usuario.getRol() == Rol.ADMIN && !usuarioRepository.existsByRolAndActivoTrueAndIdNot(Rol.ADMIN,id))
            throw new ApiException(HttpStatus.CONFLICT, "Debe permanecer al menos un administrador activo.");
        if (usuario.getRol() == Rol.TECNICO && agenda.existsByTecnico_IdAndEstadoIn(id,
                List.of(cl.rollerapp.backend.model.enums.EstadoAgenda.DISPONIBLE,
                        cl.rollerapp.backend.model.enums.EstadoAgenda.PRE_RESERVADO,
                        cl.rollerapp.backend.model.enums.EstadoAgenda.RESERVADO)))
            throw new ApiException(HttpStatus.CONFLICT, "Reasigna o cierra los horarios del técnico antes de cambiar su rol.");
        if (usuario.getRol() == Rol.VENDEDOR && pedidos.existsByVendedor_IdAndEstadoNotIn(id,
                List.of(cl.rollerapp.backend.model.enums.EstadoPedido.REALIZADO,
                        cl.rollerapp.backend.model.enums.EstadoPedido.CANCELADO)))
            throw new ApiException(HttpStatus.CONFLICT, "Reasigna o finaliza los pedidos del vendedor antes de cambiar su rol.");
        usuario.setRol(rol);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    private Usuario obtenerUsuarioActivoOFallar(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El usuario ya no existe o fue suspendido.");
        }
        return usuario;
    }

    private String generarCodigoSeisDigitos() {
        return String.valueOf(100000 + ALEATORIO.nextInt(900000));
    }
}
