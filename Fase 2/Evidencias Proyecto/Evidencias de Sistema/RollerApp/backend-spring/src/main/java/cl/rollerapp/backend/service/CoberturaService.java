package cl.rollerapp.backend.service;

import cl.rollerapp.backend.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class CoberturaService {
    private final Set<String> comunasHabilitadas;

    public CoberturaService(@Value("${app.cobertura.comunas-habilitadas:}") String comunas) {
        this.comunasHabilitadas = Arrays.stream(comunas.split(","))
                .map(CoberturaService::normalizar)
                .filter(valor -> !valor.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public void validarComuna(String comuna) {
        if (!comunasHabilitadas.contains(normalizar(comuna))) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Por ahora no realizamos visitas técnicas en esta comuna. Contáctanos directamente para evaluar tu solicitud.");
        }
    }

    private static String normalizar(String comuna) {
        if (comuna == null) return "";
        String sinTildes = Normalizer.normalize(comuna.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinTildes.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
