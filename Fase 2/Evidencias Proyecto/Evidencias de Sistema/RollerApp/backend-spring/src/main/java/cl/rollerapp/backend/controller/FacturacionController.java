package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class FacturacionController {
    private final JdbcTemplate db;
    public record Campo(Long id, String clave, String etiqueta, String tipo, boolean obligatorio, boolean activo) {}
    public record GuardarCampo(@NotBlank @Pattern(regexp="[a-z][a-z0-9_]{0,49}") String clave,
        @NotBlank @Size(max=100) String etiqueta, @NotBlank @Pattern(regexp="TEXTO|EMAIL|NUMERO") String tipo,
        @NotNull Boolean obligatorio, @NotNull Boolean activo) {}
    public record Datos(Map<String,String> valores) {}
    private boolean administrador(UsuarioPrincipal p) { return p.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); }
    private List<Campo> campos(boolean todos) {
        return db.query("SELECT id, clave, etiqueta, tipo, obligatorio, activo FROM facturacion_campos" + (todos ? "" : " WHERE activo=1") + " ORDER BY id",
            (rs,n) -> new Campo(rs.getLong("id"),rs.getString("clave"),rs.getString("etiqueta"),rs.getString("tipo"),rs.getBoolean("obligatorio"),rs.getBoolean("activo")));
    }
    @GetMapping("/campos")
    public List<Campo> listar(@AuthenticationPrincipal UsuarioPrincipal p) { return campos(administrador(p)); }
    @PostMapping("/campos")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Campo> crear(@Valid @RequestBody GuardarCampo c) {
        if (db.queryForObject("SELECT COUNT(*) FROM facturacion_campos WHERE clave=?",Long.class,c.clave()) > 0)
            throw new ApiException(HttpStatus.CONFLICT,"La clave ya existe.");
        db.update("INSERT INTO facturacion_campos(clave,etiqueta,tipo,obligatorio,activo) VALUES(?,?,?,?,?)",c.clave(),c.etiqueta(),c.tipo(),c.obligatorio(),c.activo());
        return campos(true);
    }
    @PutMapping("/campos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<Campo> editar(@PathVariable Long id,@Valid @RequestBody GuardarCampo c) {
        var existentes = campos(true).stream().filter(x -> x.id().equals(id)).findFirst().orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"Campo no encontrado."));
        if (!existentes.clave().equals(c.clave()) || !existentes.tipo().equals(c.tipo())) throw new ApiException(HttpStatus.CONFLICT,"La clave y el tipo son permanentes. Desactiva el campo y crea otro si necesitas cambiarlos.");
        db.update("UPDATE facturacion_campos SET etiqueta=?, obligatorio=?, activo=? WHERE id=?",c.etiqueta(),c.obligatorio(),c.activo(),id);
        return campos(true);
    }
    private Datos consultar(Long clienteId) {
        var valores = new LinkedHashMap<String,String>();
        db.query("SELECT c.clave, v.valor FROM facturacion_valores v JOIN facturacion_campos c ON c.id=v.campo_id WHERE v.cliente_id=? AND c.activo=1",rs -> { while(rs.next()) valores.put(rs.getString("clave"),rs.getString("valor")); return valores; },clienteId);
        return new Datos(valores);
    }
    @GetMapping("/mis-datos")
    public Datos propios(@AuthenticationPrincipal UsuarioPrincipal p) { return consultar(p.getId()); }
    @GetMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public Datos cliente(@PathVariable Long id) {
        if (db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE id=?",Long.class,id)==0) throw new ApiException(HttpStatus.NOT_FOUND,"Cliente no encontrado.");
        return consultar(id);
    }
    @PutMapping("/mis-datos")
    @Transactional
    public Datos guardar(@AuthenticationPrincipal UsuarioPrincipal p, @RequestBody Datos datos) {
        if (datos.valores()==null || datos.valores().size()>100) throw new ApiException(HttpStatus.BAD_REQUEST,"Datos inválidos.");
        db.queryForObject("SELECT id FROM usuarios WHERE id=? FOR UPDATE",Long.class,p.getId());
        var activos = campos(false);
        var claves = activos.stream().map(Campo::clave).toList();
        if (datos.valores().keySet().stream().anyMatch(k -> !claves.contains(k))) throw new ApiException(HttpStatus.BAD_REQUEST,"Hay campos desconocidos o inactivos.");
        for (Campo c : activos) {
            String valor = Optional.ofNullable(datos.valores().get(c.clave())).orElse("").trim();
            if (c.obligatorio() && valor.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST,"Completa " + c.etiqueta());
            if (valor.length()>500) throw new ApiException(HttpStatus.BAD_REQUEST,"El valor excede 500 caracteres.");
            if (!valor.isEmpty() && c.tipo().equals("EMAIL") && !valor.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new ApiException(HttpStatus.BAD_REQUEST,"Correo de facturación inválido.");
            if (!valor.isEmpty() && c.tipo().equals("NUMERO") && !valor.matches("-?[0-9]+(\\.[0-9]+)?")) throw new ApiException(HttpStatus.BAD_REQUEST,"Valor numérico inválido.");
            if (valor.isEmpty()) db.update("DELETE FROM facturacion_valores WHERE cliente_id=? AND campo_id=?",p.getId(),c.id());
            else db.update("INSERT INTO facturacion_valores(cliente_id,campo_id,valor) VALUES(?,?,?) ON DUPLICATE KEY UPDATE valor=VALUES(valor)",p.getId(),c.id(),valor);
        }
        return consultar(p.getId());
    }
}
