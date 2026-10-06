package cl.rollerapp.backend.controller;

import cl.rollerapp.backend.dto.pago.ConfirmarPagoResponse;
import cl.rollerapp.backend.dto.pago.IniciarPagoRequest;
import cl.rollerapp.backend.dto.pago.IniciarPagoResponse;
import cl.rollerapp.backend.security.UsuarioPrincipal;
import cl.rollerapp.backend.service.PagoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

    private final PagoService pagoService;

    @Value("${app.frontend-url-pago-retorno}")
    private String urlRetornoFrontend;

    @GetMapping("/webpay/abrir")
    public void abrirWebpay(@RequestParam String token, @RequestParam String url, HttpServletResponse response) throws IOException {
        var destino = java.net.URI.create(url);
        if (!"https".equals(destino.getScheme()) || !java.util.Set.of("webpay3g.transbank.cl","webpay3gint.transbank.cl").contains(destino.getHost()) || destino.getUserInfo() != null || (destino.getPort() != -1 && destino.getPort() != 443) || token.isBlank() || token.length()>200) {
            response.sendError(400,"Destino de pago inválido."); return;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Referrer-Policy","no-referrer");
        response.getWriter().write("<!doctype html><html lang=\"es\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Webpay</title><form method=\"post\" action=\""+escapar(url)+"\"><input type=\"hidden\" name=\"token_ws\" value=\""+escapar(token)+"\"><button>Pagar con Webpay</button></form><script>document.forms[0].submit()</script></html>");
    }

    @PostMapping("/iniciar")
    public IniciarPagoResponse iniciar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody IniciarPagoRequest req
    ) {
        return pagoService.iniciarPago(principal.getId(), req.cotizacionId());
    }

    @PostMapping("/carrito/iniciar")
    public IniciarPagoResponse iniciarCarrito(@AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody cl.rollerapp.backend.dto.pago.IniciarCarritoRequest req) {
        return pagoService.iniciarCarrito(principal.getId(), req);
    }

    @RequestMapping(value = "/webpay/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public void recibirRetornoWebpay(
            @RequestParam(value = "token_ws", required = false) String tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) String tokenRechazo,
            HttpServletResponse response
    ) throws IOException {
        boolean aprobado = false;
        String detalle;
        ConfirmarPagoResponse resultado = null;
        if (tokenWs != null && !tokenWs.isBlank() && (tokenRechazo == null || tokenRechazo.isBlank())) {
            try {
                resultado = pagoService.confirmarRetornoWebpay(tokenWs);
                aprobado = resultado.aprobado();
                detalle = aprobado ? "Tu pago fue realizado con éxito. Revisa el estado de tu compra en Mis pedidos."
                        : "El pago no fue autorizado. Vuelve a RollerApp para revisar tu cotización.";
            } catch (RuntimeException ex) {
                log.error("No se pudo procesar el retorno de Webpay", ex);
                detalle = "No pudimos verificar el pago en este momento. Vuelve a RollerApp y contacta a soporte antes de intentarlo otra vez.";
            }
        } else if (tokenRechazo != null && !tokenRechazo.isBlank()) {
            resultado = pagoService.anularRetornoWebpay(tokenRechazo);
            aprobado = resultado.aprobado();
            detalle = aprobado ? "Tu pago fue confirmado. Revisa Mis pedidos." : "El pago fue anulado y el horario reservado se liberó. Puedes elegir otro horario.";
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el retorno de Webpay.");
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        String tokenRetorno = tokenRechazo != null && !tokenRechazo.isBlank() ? tokenRechazo : tokenWs;
        String consulta = "?token=" + java.net.URLEncoder.encode(tokenRetorno, java.nio.charset.StandardCharsets.UTF_8)
                + (!aprobado && tokenRechazo != null ? "&estado=anulado" : "");
        String enlaceApp = "rollerapp://pago/retorno" + consulta;
        String enlaceWeb = urlRetornoFrontend + consulta;
        String origenWeb = urlRetornoFrontend.replaceFirst("/pago/retorno/?$", "").replaceAll("/+$", "");
        String detalles = resultado == null ? "" : "<dl>"
                + dato("Orden de compra", resultado.ordenCompra())
                + dato("Monto", resultado.monto() == null ? null : "$" + resultado.monto().toPlainString() + " CLP")
                + dato("Pedidos", resultado.pedidoIds().stream().map(id -> "#" + id).collect(java.util.stream.Collectors.joining(", ")))
                + dato("Tarjeta terminada en", resultado.tarjetaUltimosDigitos())
                + "</dl>";
        response.getWriter().write("<!doctype html><html lang=\"es\"><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Resultado de pago | RollerApp</title><style>body{font:18px system-ui;background:#f6f7fb;color:#1e293b;margin:0}"
                + "main{max-width:38rem;margin:5vh auto;padding:2rem;background:white;border-radius:1rem;box-shadow:0 8px 30px #15233a1a}"
                + "h1{color:" + (aprobado ? "#087854" : "#a13832") + "}dt{font-weight:700;margin-top:1rem}dd{margin:.25rem 0}"
                + "a{display:inline-block;background:#174ea6;color:white;padding:.75rem 1rem;border-radius:.5rem;text-decoration:none;margin:.5rem .5rem .5rem 0}"
                + "a.secundario{background:#e8edf5;color:#174ea6}</style><main><h1>"
                + (aprobado ? "¡Pago realizado con éxito!" : "Resultado del pago") + "</h1><p>" + detalle + "</p>"
                + detalles + "<nav><a href=\"" + escapar(enlaceApp) + "\">Volver a la aplicación</a><a href=\"" + escapar(enlaceWeb) + "\">Continuar en la web</a><a href=\"" + escapar(origenWeb + "/mis-pedidos") + "\">Ver mis pedidos</a>"
                + "<a class=\"secundario\" href=\"" + escapar(origenWeb + "/catalogo") + "\">Ir al inicio</a>"
                + "</nav></main></html>");
    }

    private static String dato(String etiqueta, String valor) {
        return valor == null || valor.isBlank() ? "" : "<dt>" + etiqueta + "</dt><dd>" + escapar(valor) + "</dd>";
    }

    private static String escapar(String valor) {
        return valor.replace("&", "&amp;").replace("\"", "&quot;")
                .replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }

    @PostMapping("/confirmar")
    public ConfirmarPagoResponse confirmar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody java.util.Map<String, String> body
    ) {
        return pagoService.confirmarPago(principal.getId(), body.get("tokenWebpay"));
    }
}
