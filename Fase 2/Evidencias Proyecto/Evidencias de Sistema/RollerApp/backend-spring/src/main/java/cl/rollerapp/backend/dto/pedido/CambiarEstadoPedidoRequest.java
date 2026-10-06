package cl.rollerapp.backend.dto.pedido;

import cl.rollerapp.backend.model.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPedidoRequest(@NotNull(message = "Falta indicar el nuevo estado.") EstadoPedido estado) {
}
