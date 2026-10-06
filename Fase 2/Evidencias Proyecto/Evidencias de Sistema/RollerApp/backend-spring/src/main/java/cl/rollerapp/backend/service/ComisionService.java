package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.comision.ComisionResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.*;
import cl.rollerapp.backend.model.enums.*;
import cl.rollerapp.backend.repository.ComisionRepository;
import cl.rollerapp.backend.repository.CotizacionServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComisionService {
    private final ComisionRepository repository;
    private final CotizacionServicioRepository cotizacionServicioRepository;

    
    @Value("${app.comisiones.porcentaje-vendedor:0}")
    private BigDecimal porcentajeVendedor;

    @Transactional
    public void generarAlFinalizar(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.REALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "RH-08: solo los pedidos realizados generan comisiones.");
        }
        
        if (pedido.getVendedor() != null) {
            BigDecimal productos = pedido.getCotizacion().getValorTela()
                    .add(pedido.getCotizacion().getValorMecanismo());
            generarVendedor(pedido, productos);
        }
        
        if (pedido.getAgendaBloque() != null
                && pedido.getAgendaBloque().getEstado() == EstadoAgenda.COMPLETADO
                && pedido.getAgendaBloque().getTecnico() != null) {
            BigDecimal monto = cotizacionServicioRepository.findByCotizacion_Id(pedido.getCotizacion().getId())
                    .stream().map(cs -> cs.getComisionTecnicoAplicada() == null
                            ? BigDecimal.ZERO : cs.getComisionTecnicoAplicada())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (monto.signum() > 0) {
                guardarSiNoExiste(pedido, pedido.getAgendaBloque().getTecnico(), Rol.TECNICO,
                        null, monto);
            }
        }
    }

    private void generarVendedor(Pedido pedido, BigDecimal productos) {
        if (porcentajeVendedor == null || porcentajeVendedor.signum() < 0
                || porcentajeVendedor.compareTo(new BigDecimal("100")) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Porcentaje de comisión inválido para vendedor.");
        }
        if (porcentajeVendedor.signum() == 0 || productos == null || productos.signum() <= 0) return;
        BigDecimal monto = productos.multiply(porcentajeVendedor)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        guardarSiNoExiste(pedido, pedido.getVendedor(), Rol.VENDEDOR,
                porcentajeVendedor.setScale(2, RoundingMode.HALF_UP), monto);
    }

    private void guardarSiNoExiste(Pedido pedido, Usuario usuario, Rol rol,
                                   BigDecimal porcentaje, BigDecimal monto) {
        if (usuario.getRol() != rol) {
            throw new ApiException(HttpStatus.CONFLICT, "El usuario asignado no tiene el rol " + rol + ".");
        }
        if (repository.existsByPedido_IdAndUsuario_Id(pedido.getId(), usuario.getId())) return;
        repository.save(Comision.builder().pedido(pedido).usuario(usuario)
                .porcentaje(porcentaje).monto(monto.setScale(2, RoundingMode.HALF_UP))
                .estado(EstadoComision.LIQUIDABLE).build());
    }

    @Transactional(readOnly = true)
    public List<ComisionResponse> listar(EstadoComision estado, Long usuarioId) {
        return repository.listar(estado, usuarioId).stream().map(ComisionResponse::desde).toList();
    }

    @Transactional
    public ComisionResponse liquidar(Long id) {
        Comision c = repository.buscarParaLiquidar(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comisión no encontrada."));
        if (c.getPedido().getEstado() != EstadoPedido.REALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se liquidan comisiones de pedidos realizados.");
        }
        if (c.getEstado() != EstadoComision.LIQUIDABLE) {
            throw new ApiException(HttpStatus.CONFLICT, "La comisión ya está pagada.");
        }
        c.setEstado(EstadoComision.PAGADA);
        c.setPagadaEn(LocalDateTime.now());
        return ComisionResponse.desde(repository.save(c));
    }
}
