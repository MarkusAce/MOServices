package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.cotizacion.CotizacionResponse;
import cl.rollerapp.backend.dto.cotizacion.CrearCotizacionRequest;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.*;
import cl.rollerapp.backend.model.enums.EstadoCotizacion;
import cl.rollerapp.backend.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final CotizacionServicioRepository cotizacionServicioRepository;
    private final ProductoRepository productoRepository;
    private final TelaRepository telaRepository;
    private final MecanismoRepository mecanismoRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CoberturaService coberturaService;
    private final DireccionesService direccionesService;
    private final org.springframework.context.ApplicationEventPublisher eventos;
    private final CotizacionVigenciaService vigenciaService;
    @PersistenceContext
        private EntityManager entityManager;

    @Transactional
    public CotizacionResponse crear(Long clienteId, CrearCotizacionRequest req) {
        return crearVerificandoDireccion(clienteId, req, false);
    }

    private CotizacionResponse crearVerificandoDireccion(Long clienteId, CrearCotizacionRequest req, boolean renovacionLegacy) {
        String direccion = req.direccion();
        String comuna = req.comuna();
        if (direccionesService.configurado() && !renovacionLegacy && !req.direccionManual()) {
            if (req.placeId() == null || req.placeId().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Selecciona una dirección sugerida antes de cotizar.");
            }
            var verificada = direccionesService.verificar(req.placeId(), null);
            direccion = verificada.direccion();
            comuna = verificada.comuna();
        }
        coberturaService.validarComuna(comuna);
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        Producto producto = productoRepository.findById(req.productoId())
                .filter(Producto::getActivo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto seleccionado no existe o ya no está disponible."));

        Tela tela = telaRepository.findById(req.telaId())
                .filter(Tela::getActivo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La tela seleccionada no existe o ya no está disponible."));

        Mecanismo mecanismo = mecanismoRepository.findById(req.mecanismoId())
                .filter(Mecanismo::getActivo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El mecanismo seleccionado no existe o ya no está disponible."));

        if (req.anchoCm().signum() <= 0 || req.altoCm().signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Las medidas deben ser mayores a cero.");
        }

        if (req.anchoCm().compareTo(producto.getAnchoMaxCm()) > 0 || req.altoCm().compareTo(producto.getAltoMaxCm()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Esa medida supera el máximo de catálogo para este producto (" +
                    producto.getAnchoMaxCm() + " x " + producto.getAltoMaxCm() + " cm). Contáctanos para confección especial.");
        }

        BigDecimal metrosCuadrados = req.anchoCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                .multiply(req.altoCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        
        BigDecimal metrosCuadradosFacturados = metrosCuadrados.max(BigDecimal.ONE);
        BigDecimal valorTela = metrosCuadradosFacturados.multiply(tela.getPrecioM2()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorMecanismo = mecanismo.getValorFijo();

        if (req.servicioIds() != null && new HashSet<>(req.servicioIds()).size() != req.servicioIds().size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede seleccionar el mismo servicio más de una vez.");
        }

        List<Servicio> serviciosSeleccionados = (req.servicioIds() == null || req.servicioIds().isEmpty())
                ? List.of()
                : servicioRepository.findAllById(req.servicioIds());

        if (serviciosSeleccionados.size() != (req.servicioIds() == null ? 0 : req.servicioIds().size())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Uno de los servicios seleccionados no existe.");
        }

        if (serviciosSeleccionados.stream().anyMatch(s -> !Boolean.TRUE.equals(s.getActivo()))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uno de los servicios seleccionados no está disponible.");
        }

        BigDecimal valorServicios = serviciosSeleccionados.stream()
                .map(Servicio::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Cotizacion cotizacion = Cotizacion.builder()
                .cliente(cliente)
                .producto(producto)
                .tela(tela)
                .mecanismo(mecanismo)
                .anchoCm(req.anchoCm())
                .altoCm(req.altoCm())
                .valorTela(valorTela)
                .valorMecanismo(valorMecanismo)
                .valorServicios(valorServicios)
                .direccion(direccion)
                .comuna(comuna)
                .placeId(req.direccionManual() ? null : req.placeId())
                .direccionPendienteVerificacion(req.direccionManual() || req.placeId() == null || renovacionLegacy)
                .referenciasDireccion(req.referenciasDireccion())
                .estado(EstadoCotizacion.PENDIENTE_PAGO)
                .build();

        Cotizacion guardada = cotizacionRepository.save(cotizacion);
        entityManager.flush();      
        entityManager.refresh(guardada); 

        guardada.getServicios().size(); 
        serviciosSeleccionados.forEach(servicio -> {
            CotizacionServicio cs = new CotizacionServicio();
            cs.setId(new CotizacionServicioId(guardada.getId(), servicio.getId()));
            cs.setCotizacion(guardada);
            cs.setServicio(servicio);
            cs.setPrecioAplicado(servicio.getPrecio());
            cs.setComisionTecnicoAplicada(servicio.getComisionTecnico() == null ? BigDecimal.ZERO : servicio.getComisionTecnico());
            cotizacionServicioRepository.save(cs);
            guardada.getServicios().add(cs);
        });

        eventos.publishEvent(new NotificacionTransaccional(cliente.getCorreo(),
                "Cotización #" + guardada.getId() + " — RollerApp",
                "Hola " + cliente.getNombre() + ", tu cotización está guardada.\n"
                + "Producto: " + producto.getNombre() + "\nMedidas: " + req.anchoCm() + " x " + req.altoCm() + " cm\n"
                + "Tela: " + tela.getNombre() + "\nMecanismo: " + mecanismo.getNombre() + "\n"
                + "Total: $" + guardada.getTotal() + " CLP\nVigencia: 15 días corridos.\n"
                + "Consulta Mis pedidos para retomar la compra. Esta cotización no acredita un pago."));
        return CotizacionResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtenerPorId(Long id, Long clienteIdQueConsulta, boolean esAdmin) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));

        if (!esAdmin && !cotizacion.getCliente().getId().equals(clienteIdQueConsulta)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes ver la cotización de otro cliente.");
        }

        
        if (vigenciaService.expirarSiVencida(id)) cotizacion.setEstado(EstadoCotizacion.EXPIRADA);
        return CotizacionResponse.desde(cotizacion);
    }

    
    @Transactional
    public CotizacionResponse renovar(Long id, Long clienteId) {
        vigenciaService.expirarSiVencida(id);
        Cotizacion original = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cotización no encontrada."));
        if (!original.getCliente().getId().equals(clienteId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta cotización no te pertenece.");
        }
        if (original.getEstado() != EstadoCotizacion.EXPIRADA) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden recalcular cotizaciones expiradas.");
        }
        List<Long> servicioIds = cotizacionServicioRepository.findByCotizacion_Id(id).stream()
                .map(cs -> cs.getServicio().getId()).toList();
        return crearVerificandoDireccion(clienteId, new CrearCotizacionRequest(
                original.getProducto().getId(), original.getTela().getId(), original.getMecanismo().getId(),
                original.getAnchoCm(), original.getAltoCm(), original.getDireccion(), original.getComuna(),
                servicioIds, original.getPlaceId(), original.getPlaceId() == null, original.getReferenciasDireccion()), original.getPlaceId() == null);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CotizacionResponse> listarParaAdmin(int pagina, int cantidad) {
        
        int paginaSegura = Math.max(0, pagina);
        int cantidadSegura = Math.max(1, Math.min(50, cantidad));
        return cotizacionRepository.findAllByOrderByIdDesc(
                org.springframework.data.domain.PageRequest.of(paginaSegura, cantidadSegura))
                .map(CotizacionResponse::desde);
    }

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listarPropias(Long clienteId) {
        
        
        vigenciaService.expirarPendientesCliente(clienteId);
        return cotizacionRepository.findByCliente_IdOrderByIdDesc(clienteId).stream()
                .map(CotizacionResponse::desde)
                .toList();
    }
}
