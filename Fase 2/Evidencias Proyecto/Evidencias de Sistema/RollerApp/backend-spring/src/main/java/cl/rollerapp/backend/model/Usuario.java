package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "ultimo_acceso_en")
    private LocalDateTime ultimoAccesoEn;

    @Column(name = "anonimizado_en")
    private LocalDateTime anonimizadoEn;

    

    @Column(name = "codigo_verificacion", length = 10)
    private String codigoVerificacion;

    @Column(name = "codigo_expira")
    private LocalDateTime codigoExpira;

    @Column(name = "cambio_contrasena_autorizado_hasta")
    private LocalDateTime cambioContrasenaAutorizadoHasta;

    @Column(name = "correo_pendiente", length = 150)
    private String correoPendiente;

    @Column(name = "codigo_cambio_correo", length = 10)
    private String codigoCambioCorreo;

    @Column(name = "codigo_cambio_correo_expira")
    private LocalDateTime codigoCambioCorreoExpira;

    @Column(name = "codigo_recuperacion", length = 10)
    private String codigoRecuperacion;

    @Column(name = "codigo_recuperacion_expira")
    private LocalDateTime codigoRecuperacionExpira;

    @Column(name = "reset_nonce", length = 64)
    private String resetNonce;

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
        if (rol == null) rol = Rol.CLIENTE;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (ultimoAccesoEn == null) ultimoAccesoEn = creadoEn;
    }
}
