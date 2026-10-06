package cl.rollerapp.backend.dto.pedido;

import jakarta.validation.constraints.NotNull;

public record AsignarVendedorRequest(@NotNull Long vendedorId) {}
