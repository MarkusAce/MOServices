package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.MensajeRespuesta;
import cl.rollerapp.backend.dto.auth.*;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final cl.rollerapp.backend.service.AuditoriaAdministrativaService auditoria;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeRespuesta registrar(@Valid @RequestBody RegistroRequest req) {
        return authService.registrar(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return authService.obtenerPerfil(principal.getId());
    }

    @PutMapping("/perfil")
    public UsuarioResponse actualizarPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody ActualizarPerfilRequest req
    ) {
        return authService.actualizarPerfil(principal.getId(), req);
    }

    @PostMapping("/verificar-contrasena-actual")
    public MensajeRespuesta verificarContrasenaActual(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody VerificarContrasenaActualRequest req
    ) {
        authService.verificarContrasenaActual(principal.getId(), req.contrasenaActual());
        return new MensajeRespuesta("Contraseña verificada.");
    }

    @PostMapping("/enviar-codigo")
    public EnviarCodigoResponse enviarCodigo(@AuthenticationPrincipal UsuarioPrincipal principal,
                                            @Valid @RequestBody VerificarContrasenaActualRequest req) {
        return authService.enviarCodigoVerificacion(principal.getId(), req.contrasenaActual());
    }

    @PostMapping("/verificar-codigo")
    public MensajeRespuesta verificarCodigo(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody VerificarCodigoRequest req
    ) {
        authService.verificarCodigo(principal.getId(), req.codigo());
        return new MensajeRespuesta("Código verificado.");
    }

    @PutMapping("/cambiar-contrasena")
    public MensajeRespuesta cambiarContrasena(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody CambiarContrasenaRequest req
    ) {
        return authService.cambiarContrasena(principal.getId(), req.nuevaContrasena());
    }

    @PostMapping("/solicitar-cambio-correo")
    public MensajeRespuesta solicitarCambioCorreo(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                  @Valid @RequestBody SolicitarCambioCorreoRequest req) {
        return authService.solicitarCambioCorreo(principal.getId(), req.contrasenaActual(), req.nuevoCorreo());
    }

    @PostMapping("/confirmar-cambio-correo")
    public UsuarioResponse confirmarCambioCorreo(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                  @Valid @RequestBody VerificarCodigoRequest req) {
        return authService.confirmarCambioCorreo(principal.getId(), req.codigo());
    }

    @PostMapping("/olvide-contrasena")
    public OlvideContrasenaResponse olvideContrasena(@Valid @RequestBody OlvideContrasenaRequest req) {
        return authService.olvideContrasena(req.correo());
    }

    @PostMapping("/verificar-codigo-recuperacion")
    public VerificarCodigoRecuperacionResponse verificarCodigoRecuperacion(
            @Valid @RequestBody VerificarCodigoRecuperacionRequest req
    ) {
        return authService.verificarCodigoRecuperacion(req.correo(), req.codigo());
    }

    @PutMapping("/restablecer-contrasena")
    public MensajeRespuesta restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest req) {
        return authService.restablecerContrasena(req.resetToken(), req.nuevaContrasena());
    }

    @PostMapping("/usuarios/personal")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearPersonal(@Valid @RequestBody CrearPersonalRequest req,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        UsuarioResponse respuesta = authService.crearPersonal(req);
        auditoria.registrar(principal.getId(), "CREAR_PERSONAL", "USUARIO", respuesta.id(), req.rol().name());
        return respuesta;
    }

    @PutMapping("/usuarios/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest req,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        UsuarioResponse respuesta = authService.cambiarRol(id, req.rol(), principal.getId());
        auditoria.registrar(principal.getId(), "CAMBIAR_ROL", "USUARIO", id, req.rol().name());
        return respuesta;
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponse> listarUsuarios() {
        return authService.listarUsuarios();
    }

    @PutMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse cambiarEstadoUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody ActualizarEstadoUsuarioRequest req
    ) {
        UsuarioResponse actualizado = authService.cambiarEstadoUsuario(id, Boolean.TRUE.equals(req.activo()));
        auditoria.registrar(principal.getId(), "CAMBIAR_ESTADO_USUARIO", "USUARIO", id, Boolean.TRUE.equals(req.activo()) ? "ACTIVAR" : "DESACTIVAR");
        return actualizado;
    }
}
