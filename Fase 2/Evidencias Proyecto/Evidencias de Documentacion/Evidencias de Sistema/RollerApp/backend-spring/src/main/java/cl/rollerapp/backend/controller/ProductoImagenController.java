package cl.rollerapp.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/productos/imagenes")
public class ProductoImagenController {
    private final Path directorio;

    public ProductoImagenController(@Value("${app.imagenes.directorio:./uploads/productos}") String ruta) throws IOException {
        directorio = Paths.get(ruta).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty() || archivo.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen de hasta 5 MB.");
        }
        String tipo = archivo.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equals(tipo) && !MediaType.IMAGE_PNG_VALUE.equals(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes JPG o PNG.");
        }
        try (InputStream entrada = archivo.getInputStream()) {
            if (ImageIO.read(entrada) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no es una imagen válida.");
            }
        }
        String nombre = UUID.randomUUID() + (MediaType.IMAGE_PNG_VALUE.equals(tipo) ? ".png" : ".jpg");
        Path destino = directorio.resolve(nombre);
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(archivo.getInputStream(), destino);
        }
        return Map.of("imagenPrincipal", "/api/productos/imagenes/" + nombre);
    }

    @GetMapping("/{nombre:.+}")
    public ResponseEntity<Resource> obtener(@PathVariable String nombre) throws MalformedURLException {
        if (!nombre.matches("[0-9a-fA-F-]{36}\\.(jpg|png)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.getParent().equals(directorio) || !Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MediaType tipo = nombre.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo)
                .cacheControl(CacheControl.noCache())
                .body(new UrlResource(archivo.toUri()));
    }
}
