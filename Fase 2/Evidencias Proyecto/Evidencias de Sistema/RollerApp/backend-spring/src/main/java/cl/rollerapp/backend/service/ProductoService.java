package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.producto.CrearProductoRequest;
import cl.rollerapp.backend.dto.producto.ProductoResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.Mecanismo;
import cl.rollerapp.backend.model.Producto;
import cl.rollerapp.backend.model.Tela;
import cl.rollerapp.backend.repository.MecanismoRepository;
import cl.rollerapp.backend.repository.ProductoRepository;
import cl.rollerapp.backend.repository.TelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final TelaRepository telaRepository;
    private final MecanismoRepository mecanismoRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarActivos() {
        return productoRepository.findByActivoTrue().stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return ProductoResponse.desde(buscarOFallar(id));
    }

    @Transactional
    public ProductoResponse crear(CrearProductoRequest req) {
        Producto producto = Producto.builder()
                .nombre(req.nombre())
                .descripcion(req.descripcion())
                .categoria(req.categoria())
                .imagenPrincipal(req.imagenPrincipal())
                .anchoMaxCm(req.anchoMaxCm() != null ? req.anchoMaxCm() : new BigDecimal("200"))
                .altoMaxCm(req.altoMaxCm() != null ? req.altoMaxCm() : new BigDecimal("260"))
                .activo(true)
                .build();

        if (req.telaDefectoId() != null) {
            producto.setTelaDefecto(telaRepository.findById(req.telaDefectoId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La tela indicada no existe.")));
        }
        if (req.mecanismoDefectoId() != null) {
            producto.setMecanismoDefecto(mecanismoRepository.findById(req.mecanismoDefectoId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El mecanismo indicado no existe.")));
        }

        return ProductoResponse.desde(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse editar(Long id, CrearProductoRequest req) {
        Producto producto = buscarOFallar(id);
        producto.setNombre(req.nombre());
        producto.setDescripcion(req.descripcion());
        producto.setCategoria(req.categoria());
        producto.setImagenPrincipal(req.imagenPrincipal());
        if (req.anchoMaxCm() != null) producto.setAnchoMaxCm(req.anchoMaxCm());
        if (req.altoMaxCm() != null) producto.setAltoMaxCm(req.altoMaxCm());
        producto.setTelaDefecto(req.telaDefectoId() == null ? null : telaRepository.findById(req.telaDefectoId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La tela indicada no existe.")));
        producto.setMecanismoDefecto(req.mecanismoDefectoId() == null ? null : mecanismoRepository.findById(req.mecanismoDefectoId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El mecanismo indicado no existe.")));
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizarEstado(Long id, boolean activo) {
        Producto producto = buscarOFallar(id);
        producto.setActivo(activo);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    private Producto buscarOFallar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
    }
}
