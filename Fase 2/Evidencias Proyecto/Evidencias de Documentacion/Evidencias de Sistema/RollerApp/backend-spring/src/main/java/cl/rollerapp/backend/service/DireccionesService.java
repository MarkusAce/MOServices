package cl.rollerapp.backend.service;

import cl.rollerapp.backend.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service @Slf4j
public class DireccionesService {
    private final RestClient google = RestClient.builder().baseUrl("https://places.googleapis.com").build();
    private final String apiKey;
    private final CoberturaService cobertura;

    public record Sugerencia(String placeId, String texto) { }
    public record DireccionVerificada(String placeId, String direccion, String comuna,
                                     double latitud, double longitud) { }

    public DireccionesService(@Value("${app.direcciones.google-api-key:}") String apiKey,
                              CoberturaService cobertura) {
        this.apiKey = apiKey;
        this.cobertura = cobertura;
    }

    public boolean configurado() { return apiKey != null && !apiKey.isBlank(); }

    public List<Sugerencia> sugerir(String texto, String token) {
        exigirConfiguracion();
        if (texto == null || texto.trim().length() < 3 || texto.length() > 120) return List.of();
        validarToken(token);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> respuesta = google.post().uri("/v1/places:autocomplete")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "suggestions.placePrediction.placeId,suggestions.placePrediction.text.text")
                    .body(Map.of("input", texto.trim(), "sessionToken", token,
                            "includedRegionCodes", List.of("cl"), "languageCode", "es"))
                    .retrieve().body(Map.class);
            if (respuesta == null || !(respuesta.get("suggestions") instanceof List<?> lista)) return List.of();
            return lista.stream().filter(Map.class::isInstance).map(Map.class::cast)
                    .map(s -> s.get("placePrediction"))
                    .filter(Map.class::isInstance).map(Map.class::cast)
                    .filter(p -> p.get("placeId") instanceof String && p.get("text") instanceof Map)
                    .map(p -> new Sugerencia((String) p.get("placeId"),
                            String.valueOf(((Map<?, ?>) p.get("text")).get("text"))))
                    .toList();
        } catch (RuntimeException ex) {
            log.warn("No se pudo consultar el autocompletado de direcciones", ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "El servicio de direcciones no responde. Intenta nuevamente.");
        }
    }

    public DireccionVerificada verificar(String placeId, String token) {
        exigirConfiguracion();
        if (placeId == null || !placeId.matches("[A-Za-z0-9_-]{4,255}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selecciona una dirección sugerida válida.");
        }
        if (token != null && !token.isBlank()) validarToken(token);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> lugar = google.get()
                    .uri(uri -> uri.path("/v1/places/{id}").queryParam("languageCode", "es")
                            .queryParamIfPresent("sessionToken", Optional.ofNullable(token).filter(t -> !t.isBlank()))
                            .build(placeId))
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "id,formattedAddress,addressComponents,location")
                    .retrieve().body(Map.class);
            if (lugar == null || !(lugar.get("formattedAddress") instanceof String direccion)
                    || !(lugar.get("location") instanceof Map<?, ?> ubicacion)
                    || !(ubicacion.get("latitude") instanceof Number lat)
                    || !(ubicacion.get("longitude") instanceof Number lon)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La dirección seleccionada no tiene coordenadas verificables.");
            }
            
            if (lat.doubleValue() < -34.4 || lat.doubleValue() > -32.8
                    || lon.doubleValue() < -71.9 || lon.doubleValue() > -69.7) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La dirección está fuera de la cobertura metropolitana.");
            }
            if (!(lugar.get("addressComponents") instanceof List<?> componentes)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La dirección no indica una comuna verificable.");
            }
            String comuna = null;
            for (String tipo : List.of("administrative_area_level_3", "sublocality_level_1", "locality")) {
                for (Object item : componentes) {
                    if (!(item instanceof Map<?, ?> componente)
                            || !(componente.get("types") instanceof List<?> tipos) || !tipos.contains(tipo)) continue;
                    Object nombre = componente.get("longText");
                    if (!(nombre instanceof String candidata)) continue;
                    try { cobertura.validarComuna(candidata); comuna = candidata; break; }
                    catch (ApiException ignorada) {  }
                }
                if (comuna != null) break;
            }
            if (comuna == null) throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Por ahora no realizamos visitas técnicas en esta comuna. Contáctanos directamente.");
            return new DireccionVerificada(placeId, direccion, comuna,
                    lat.doubleValue(), lon.doubleValue());
        } catch (ApiException ex) { throw ex; }
        catch (RuntimeException ex) {
            log.warn("No se pudo verificar la dirección con Places", ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo verificar la dirección. Intenta nuevamente.");
        }
    }

    private void exigirConfiguracion() {
        if (!configurado()) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "La verificación de direcciones aún no está configurada.");
    }

    private void validarToken(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{20,36}"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sesión de búsqueda inválida.");
    }
}
