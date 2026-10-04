package cl.rollerapp.backend.service;

import cl.rollerapp.backend.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class WebpayService {

    private final RestClient restClient;
    private final String codigoComercio;
    private final String apiKey;
    private final String urlRetorno;

    public WebpayService(
            @Value("${webpay.codigo-comercio}") String codigoComercio,
            @Value("${webpay.api-key}") String apiKey,
            @Value("${webpay.ambiente}") String ambiente,
            @Value("${app.backend-url-publica}") String backendUrlPublica
    ) {
        this.codigoComercio = codigoComercio;
        this.apiKey = apiKey;
        this.urlRetorno = backendUrlPublica + "/api/pagos/webpay/retorno";

        String baseUrl = "produccion".equalsIgnoreCase(ambiente)
                ? "https://webpay3g.transbank.cl"
                : "https://webpay3gint.transbank.cl";

        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restClient = RestClient.builder().requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader("Tbk-Api-Key-Id", codigoComercio)
                .defaultHeader("Tbk-Api-Key-Secret", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public record RespuestaCrearTransaccion(String token, String url) {
    }

    public RespuestaCrearTransaccion crearTransaccion(String ordenCompra, String sessionId, BigDecimal monto) {
        try {
            Map<String, Object> cuerpo = Map.of(
                    "buy_order", ordenCompra,
                    "session_id", sessionId,
                    "amount", monto.intValueExact(), 
                    "return_url", urlRetorno
            );

            Map<String, Object> respuesta = restClient.post()
                    .uri("/rswebpaytransaction/api/webpay/v1.2/transactions")
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null || !(respuesta.get("token") instanceof String token) || token.isBlank()
                    || !(respuesta.get("url") instanceof String url) || url.isBlank()) {
                throw new IllegalStateException("Webpay no entregó un token o URL de pago.");
            }
            return new RespuestaCrearTransaccion(token, url);
        } catch (Exception ex) {
            log.error("Error creando transacción Webpay", ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo iniciar el pago con Webpay. Intenta de nuevo.");
        }
    }

    public record RespuestaConfirmarTransaccion(
            String estado, 
            String ordenCompra,
            BigDecimal monto,
            String codigoAutorizacion,
            String tipoPago,
            String ultimosDigitosTarjeta,
            Integer codigoRespuesta
    ) {
    }

    @SuppressWarnings("unchecked")
    public RespuestaConfirmarTransaccion confirmarTransaccion(String token) {
        try {
            Map<String, Object> respuesta = restClient.put()
                    .uri("/rswebpaytransaction/api/webpay/v1.2/transactions/{token}", token)
                    .retrieve()
                    .body(Map.class);

            return interpretarResultado(respuesta);
        } catch (Exception ex) {
            log.error("Error confirmando transacción Webpay (token {})", token, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo confirmar el pago con Webpay.");
        }
    }

    @SuppressWarnings("unchecked")
    public RespuestaConfirmarTransaccion consultarEstado(String token) {
        try {
            Map<String, Object> respuesta = restClient.get()
                    .uri("/rswebpaytransaction/api/webpay/v1.2/transactions/{token}", token)
                    .retrieve()
                    .body(Map.class);
            return interpretarResultado(respuesta);
        } catch (Exception ex) {
            log.error("Error consultando estado de transacción Webpay (token {})", token, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo consultar el estado del pago con Webpay.");
        }
    }

    @SuppressWarnings("unchecked")
    private RespuestaConfirmarTransaccion interpretarResultado(Map<String, Object> respuesta) {
            if (respuesta == null) throw new IllegalStateException("Webpay no devolvió datos de la transacción.");
            Map<String, Object> detalleTarjeta = (Map<String, Object>) respuesta.get("card_detail");
            String ultimosDigitos = detalleTarjeta != null ? (String) detalleTarjeta.get("card_number") : null;

            Object montoObj = respuesta.get("amount");
            BigDecimal monto = montoObj instanceof Number n ? new BigDecimal(n.toString()) : BigDecimal.ZERO;

            return new RespuestaConfirmarTransaccion(
                    (String) respuesta.get("status"),
                    (String) respuesta.get("buy_order"),
                    monto,
                    (String) respuesta.get("authorization_code"),
                    (String) respuesta.get("payment_type_code"),
                    ultimosDigitos,
                    respuesta.get("response_code") instanceof Number n ? n.intValue() : null
            );
    }
}
