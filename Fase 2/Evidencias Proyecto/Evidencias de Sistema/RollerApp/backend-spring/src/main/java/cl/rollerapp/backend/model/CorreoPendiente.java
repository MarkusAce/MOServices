package cl.rollerapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correos_pendientes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CorreoPendiente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String destinatario;
    @Column(nullable = false, length = 200)
    private String asunto;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;
    @Column(nullable = false, length = 20)
    private String estado;
    @Column(nullable = false)
    private Integer intentos;
    @Column(name = "proximo_intento_en", nullable = false)
    private LocalDateTime proximoIntentoEn;
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;
    @PrePersist
    void prePersist() {
        if (estado == null) estado = "PENDIENTE";
        if (intentos == null) intentos = 0;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (proximoIntentoEn == null) proximoIntentoEn = creadoEn;
    }
}
