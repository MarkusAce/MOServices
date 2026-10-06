package cl.rollerapp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "telas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "paso_luz", nullable = false, length = 20)
    private PasoLuz pasoLuz;

    
    @Column(name = "precio_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioM2;

    @Column(nullable = false)
    private Boolean activo;

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
        if (pasoLuz == null) pasoLuz = PasoLuz.FILTRANTE;
    }
}
