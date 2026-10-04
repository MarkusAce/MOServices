package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.comision.ComisionResponse;
import cl.rollerapp.backend.model.enums.EstadoComision;
import cl.rollerapp.backend.service.ComisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/comisiones")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ComisionAdminController {
    private final ComisionService comisionService;

    @GetMapping
    public List<ComisionResponse> listar(@RequestParam(required = false) EstadoComision estado,
                                         @RequestParam(required = false) Long usuarioId) {
        return comisionService.listar(estado, usuarioId);
    }

    @PatchMapping("/{id}/liquidar")
    public ComisionResponse liquidar(@PathVariable Long id) {
        return comisionService.liquidar(id);
    }
}
