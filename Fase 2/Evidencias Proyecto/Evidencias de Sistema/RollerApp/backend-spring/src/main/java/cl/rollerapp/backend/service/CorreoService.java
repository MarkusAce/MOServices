package cl.rollerapp.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${app.correo.remitente}")
    private String remitente;

    public boolean enviar(String para, String asunto, String texto) {
        if (smtpHost == null || smtpHost.isBlank()) {
            log.info("[correo -- modo de prueba, sin SMTP configurado] Para: {} | Asunto: {}", para, asunto);
            return false;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        mailSender.send(mensaje);
        return true;
    }
}
