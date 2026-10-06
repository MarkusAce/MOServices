package cl.rollerapp.backend.model;

import cl.rollerapp.backend.model.enums.Categoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(name = "imagen_principal", length = 255)
    private String imagenPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tela_defecto_id")
    private Tela telaDefecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanismo_defecto_id")
    private Mecanismo mecanismoDefecto;

    @Column(name = "ancho_max_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal anchoMaxCm;

    @Column(name = "alto_max_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal altoMaxCm;

    @Column(nullable = false)
    private Boolean activo;

    @Builder.Default
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ProductoImagen> imagenes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
        if (anchoMaxCm == null) anchoMaxCm = new BigDecimal("200");
        if (altoMaxCm == null) altoMaxCm = new BigDecimal("260");
    }
}
