package cl.rollerapp.backend.service;
import cl.rollerapp.backend.model.enums.EstadoPedido;
public final class EstadoPedidoTexto {
 private EstadoPedidoTexto(){}
 public static String etiqueta(EstadoPedido estado){return switch(estado){case PAGADO->"Pagado";case EN_CONFECCION->"En producción";case EN_TERRENO->"En camino";case REALIZADO->"Finalizado";case REPROGRAMADO->"Reprogramado";case CANCELADO->"Cancelado";};}
}
