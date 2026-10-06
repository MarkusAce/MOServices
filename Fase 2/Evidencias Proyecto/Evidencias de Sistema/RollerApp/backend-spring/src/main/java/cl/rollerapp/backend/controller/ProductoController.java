package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.producto.CrearProductoRequest;
import cl.rollerapp.backend.dto.producto.ProductoResponse;
import cl.rollerapp.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<ProductoResponse> listarActivos() {
        return productoService.listarActivos();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductoResponse> listarTodos() {
        return productoService.listarTodos();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest req) {
        return productoService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse editar(@PathVariable Long id, @Valid @RequestBody CrearProductoRequest req) {
        return productoService.editar(id, req);
    }

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse archivar(@PathVariable Long id) {
        return productoService.actualizarEstado(id, false);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse actualizarEstado(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        return productoService.actualizarEstado(id, Boolean.TRUE.equals(body.get("activo")));
    }
}
