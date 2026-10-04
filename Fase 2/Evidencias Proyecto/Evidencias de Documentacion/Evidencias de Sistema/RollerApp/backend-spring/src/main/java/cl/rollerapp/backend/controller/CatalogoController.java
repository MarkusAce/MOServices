package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.producto.ActualizarTelaRequest;
import cl.rollerapp.backend.dto.producto.GuardarTelaRequest;
import cl.rollerapp.backend.dto.producto.GuardarMecanismoRequest;
import org.springframework.http.HttpStatus;
import cl.rollerapp.backend.dto.producto.MecanismoResponse;
import cl.rollerapp.backend.dto.producto.ServicioResponse;
import cl.rollerapp.backend.dto.producto.TelaResponse;
import cl.rollerapp.backend.service.CatalogoService;
import cl.rollerapp.backend.model.PasoLuz;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/api/telas")
    public List<TelaResponse> listarTelas() {
        return catalogoService.listarTelas();
    }

    @PutMapping("/api/telas/{id}/precio")
    @PreAuthorize("hasRole('ADMIN')") 
    public TelaResponse actualizarPrecioTela(@PathVariable Long id, @Valid @RequestBody ActualizarTelaRequest req) {
        return catalogoService.actualizarPrecioTela(id, req.precioM2());
    }

    @PutMapping("/api/telas/{id}/paso-luz")
    @PreAuthorize("hasRole('ADMIN')")
    public TelaResponse actualizarPasoLuz(@PathVariable Long id, @Valid @RequestBody ActualizarPasoLuzRequest req) {
        return catalogoService.actualizarPasoLuz(id, req.pasoLuz());
    }

    @GetMapping("/api/mecanismos")
    public List<MecanismoResponse> listarMecanismos() {
        return catalogoService.listarMecanismos();
    }

    @GetMapping("/api/telas/admin/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TelaResponse> todasTelas() { return catalogoService.listarTodasTelas(); }

    @PostMapping("/api/telas")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TelaResponse crearTela(@Valid @RequestBody GuardarTelaRequest req) {
        return catalogoService.guardarTela(null, req);
    }

    @PutMapping("/api/telas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TelaResponse editarTela(@PathVariable Long id, @Valid @RequestBody GuardarTelaRequest req) {
        return catalogoService.guardarTela(id, req);
    }

    @DeleteMapping("/api/telas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TelaResponse archivarTela(@PathVariable Long id) { return catalogoService.archivarTela(id); }

    @GetMapping("/api/mecanismos/admin/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MecanismoResponse> todosMecanismos() { return catalogoService.listarTodosMecanismos(); }

    @PostMapping("/api/mecanismos")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MecanismoResponse crearMecanismo(@Valid @RequestBody GuardarMecanismoRequest req) {
        return catalogoService.guardarMecanismo(null, req);
    }

    @PutMapping("/api/mecanismos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MecanismoResponse editarMecanismo(@PathVariable Long id, @Valid @RequestBody GuardarMecanismoRequest req) {
        return catalogoService.guardarMecanismo(id, req);
    }

    @DeleteMapping("/api/mecanismos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MecanismoResponse archivarMecanismo(@PathVariable Long id) {
        return catalogoService.archivarMecanismo(id);
    }

    @GetMapping("/api/servicios")
    public List<ServicioResponse> listarServicios() {
        return catalogoService.listarServicios();
    }

    @PutMapping("/api/servicios/{id}/comision-tecnico")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponse actualizarComisionTecnico(@PathVariable Long id,
            @Valid @RequestBody ActualizarComisionTecnicoRequest req) {
        return catalogoService.actualizarComisionTecnico(id, req.monto());
    }
}
