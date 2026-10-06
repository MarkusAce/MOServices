package cl.rollerapp.backend.service;

import cl.rollerapp.backend.model.enums.Rol;
import cl.rollerapp.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetencionCuentasJob {
    private final UsuarioRepository usuarios;
    private final RetencionCuentasService retencion;

    @Scheduled(cron = "0 20 3 * * *", zone = "America/Santiago")
    public void ejecutar() {
        LocalDateTime limite = LocalDateTime.now().minusYears(5);
        var candidatos = usuarios
                .findByRolAndAnonimizadoEnIsNullAndUltimoAccesoEnBeforeOrderByIdAsc(
                        Rol.CLIENTE, limite);
        for (var candidato : candidatos) {
            try {
                retencion.anonimizarSiCorresponde(candidato.getId(), limite);
            } catch (Exception ex) {
                log.error("Error al procesar retención de cuenta {}", candidato.getId(), ex);
            }
        }
    }
}
