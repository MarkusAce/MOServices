CREATE DATABASE IF NOT EXISTS rollerapp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE rollerapp;
SET NAMES utf8mb4;


-- Usuarios y seguridad
CREATE TABLE usuarios (
  id                                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre                             VARCHAR(100) NOT NULL,
  apellido                           VARCHAR(100) NOT NULL,
  correo                             VARCHAR(150) NOT NULL,
  contrasena_hash                    VARCHAR(255) NOT NULL,
  rol                                VARCHAR(20) NOT NULL DEFAULT 'cliente',
  telefono                           VARCHAR(20) NULL,
  activo                             TINYINT(1) NOT NULL DEFAULT 1,
  creado_en                          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso_en                   DATETIME NULL,
  anonimizado_en                     DATETIME NULL,
  codigo_verificacion                VARCHAR(10) NULL,
  codigo_expira                      DATETIME NULL,
  cambio_contrasena_autorizado_hasta DATETIME NULL,
  codigo_recuperacion                VARCHAR(10) NULL,
  codigo_recuperacion_expira         DATETIME NULL,
  reset_nonce                        VARCHAR(64) NULL,
  correo_pendiente                   VARCHAR(150) NULL,
  codigo_cambio_correo               VARCHAR(10) NULL,
  codigo_cambio_correo_expira        DATETIME(6) NULL,

  UNIQUE KEY uq_usuarios_correo (correo),
  KEY idx_usuarios_rol (rol)
) ENGINE=InnoDB;

CREATE TABLE auditoria_administrativa (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  actor_id    BIGINT NOT NULL,
  accion      VARCHAR(60) NOT NULL,
  recurso     VARCHAR(60) NOT NULL,
  recurso_id  BIGINT NOT NULL,
  detalle     VARCHAR(250) NULL,
  creado_en   DATETIME(6) NOT NULL
) ENGINE=InnoDB;


-- Catalogo
CREATE TABLE telas (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  precio_m2   DECIMAL(10,2) NOT NULL,
  paso_luz    VARCHAR(20) NOT NULL DEFAULT 'FILTRANTE',
  activo      TINYINT(1) NOT NULL DEFAULT 1,

  CONSTRAINT chk_telas_precio CHECK (precio_m2 >= 0)
) ENGINE=InnoDB;

CREATE TABLE mecanismos (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  valor_fijo  DECIMAL(10,2) NOT NULL,
  activo      TINYINT(1) NOT NULL DEFAULT 1,

  CONSTRAINT chk_mecanismos_valor CHECK (valor_fijo >= 0)
) ENGINE=InnoDB;

CREATE TABLE servicios (
  id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre            VARCHAR(100) NOT NULL,
  descripcion       VARCHAR(255) NULL,
  precio            DECIMAL(10,2) NOT NULL,
  comision_tecnico  DECIMAL(10,2) NOT NULL DEFAULT 0,
  activo            TINYINT(1) NOT NULL DEFAULT 1,

  CONSTRAINT chk_servicios_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE productos (
  id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre                VARCHAR(150) NOT NULL,
  descripcion           TEXT NOT NULL,
  categoria             VARCHAR(20) NOT NULL,
  imagen_principal      VARCHAR(255) NULL,
  tela_defecto_id       BIGINT UNSIGNED NULL,
  mecanismo_defecto_id  BIGINT UNSIGNED NULL,
  ancho_max_cm          DECIMAL(6,2) NOT NULL DEFAULT 200,
  alto_max_cm           DECIMAL(6,2) NOT NULL DEFAULT 260,
  activo                TINYINT(1) NOT NULL DEFAULT 1,

  CONSTRAINT fk_productos_tela
    FOREIGN KEY (tela_defecto_id) REFERENCES telas(id),
  CONSTRAINT fk_productos_mecanismo
    FOREIGN KEY (mecanismo_defecto_id) REFERENCES mecanismos(id),
  KEY idx_productos_categoria (categoria),
  KEY idx_productos_activo (activo)
) ENGINE=InnoDB;

CREATE TABLE producto_imagenes (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  producto_id BIGINT UNSIGNED NOT NULL,
  url         VARCHAR(255) NOT NULL,
  orden       INT UNSIGNED NOT NULL DEFAULT 0,

  CONSTRAINT fk_producto_imagenes_producto
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
) ENGINE=InnoDB;


-- Cotizaciones
CREATE TABLE cotizaciones (
  id                              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  cliente_id                      BIGINT UNSIGNED NOT NULL,
  producto_id                     BIGINT UNSIGNED NOT NULL,
  tela_id                         BIGINT UNSIGNED NOT NULL,
  mecanismo_id                    BIGINT UNSIGNED NOT NULL,
  ancho_cm                        DECIMAL(6,2) NOT NULL,
  alto_cm                         DECIMAL(6,2) NOT NULL,
  metros_cuadrados                DECIMAL(8,4) GENERATED ALWAYS AS
                                    ((ancho_cm / 100) * (alto_cm / 100)) STORED,
  valor_tela                      DECIMAL(10,2) NOT NULL,
  valor_mecanismo                 DECIMAL(10,2) NOT NULL,
  valor_servicios                 DECIMAL(10,2) NOT NULL DEFAULT 0,
  total                           DECIMAL(10,2) GENERATED ALWAYS AS
                                    (valor_tela + valor_mecanismo + valor_servicios) STORED,
  direccion                       VARCHAR(255) NOT NULL,
  comuna                          VARCHAR(100) NOT NULL,
  direccion_pendiente_verificacion TINYINT(1) NOT NULL DEFAULT 0,
  referencias_direccion           VARCHAR(500) NULL,
  place_id                        VARCHAR(255) NULL,
  estado                          VARCHAR(20) NOT NULL DEFAULT 'pendiente_pago',
  creado_en                       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_cotizaciones_cliente
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
  CONSTRAINT fk_cotizaciones_producto
    FOREIGN KEY (producto_id) REFERENCES productos(id),
  CONSTRAINT fk_cotizaciones_tela
    FOREIGN KEY (tela_id) REFERENCES telas(id),
  CONSTRAINT fk_cotizaciones_mecanismo
    FOREIGN KEY (mecanismo_id) REFERENCES mecanismos(id),
  CONSTRAINT chk_cotizaciones_medidas
    CHECK (ancho_cm > 0 AND alto_cm > 0),
  KEY idx_cotizaciones_cliente (cliente_id),
  KEY idx_cotizaciones_estado (estado)
) ENGINE=InnoDB;

CREATE TABLE cotizacion_servicios (
  cotizacion_id             BIGINT UNSIGNED NOT NULL,
  servicio_id               BIGINT UNSIGNED NOT NULL,
  precio_aplicado           DECIMAL(10,2) NOT NULL,
  comision_tecnico_aplicada DECIMAL(10,2) NOT NULL DEFAULT 0,

  PRIMARY KEY (cotizacion_id, servicio_id),
  CONSTRAINT fk_cotservicios_cotizacion
    FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_cotservicios_servicio
    FOREIGN KEY (servicio_id) REFERENCES servicios(id)
) ENGINE=InnoDB;


-- Agenda y visitas
CREATE TABLE agenda_bloques (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  fecha           DATE NOT NULL,
  hora_inicio     TIME NOT NULL,
  hora_fin        TIME NOT NULL,
  tecnico_id      BIGINT UNSIGNED NULL,
  cotizacion_id   BIGINT UNSIGNED NULL,
  estado          VARCHAR(20) NOT NULL DEFAULT 'disponible',
  reservado_hasta DATETIME NULL,
  atencion_inicio DATETIME NULL,
  atencion_fin    DATETIME NULL,

  CONSTRAINT fk_agenda_tecnico
    FOREIGN KEY (tecnico_id) REFERENCES usuarios(id),
  CONSTRAINT fk_agenda_cotizacion
    FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones(id),
  CONSTRAINT chk_agenda_horario
    CHECK (hora_fin > hora_inicio),
  UNIQUE KEY uq_agenda_tecnico_horario (tecnico_id, fecha, hora_inicio),
  KEY idx_agenda_estado (estado),
  KEY idx_agenda_fecha (fecha)
) ENGINE=InnoDB;

CREATE TABLE observaciones_visita (
  id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  bloque_id BIGINT UNSIGNED NOT NULL,
  autor_id  BIGINT UNSIGNED NOT NULL,
  texto     VARCHAR(2000) NOT NULL,
  creado_en TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

  KEY idx_observacion_visita_fecha (bloque_id, creado_en, id),
  CONSTRAINT fk_observacion_visita_bloque
    FOREIGN KEY (bloque_id) REFERENCES agenda_bloques(id),
  CONSTRAINT fk_observacion_visita_autor
    FOREIGN KEY (autor_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;


-- Pagos y pedidos
CREATE TABLE pagos (
  id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  cotizacion_id           BIGINT UNSIGNED NOT NULL,
  token_webpay            VARCHAR(255) NULL,
  orden_compra            VARCHAR(64) NOT NULL,
  monto                   DECIMAL(10,2) NOT NULL,
  estado_transaccion      VARCHAR(20) NOT NULL DEFAULT 'iniciada',
  codigo_autorizacion     VARCHAR(20) NULL,
  tipo_pago               VARCHAR(50) NULL,
  tarjeta_ultimos_digitos VARCHAR(4) NULL,
  fecha_transaccion       DATETIME NULL,
  creado_en               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_pagos_cotizacion
    FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones(id),
  UNIQUE KEY uq_pagos_orden_compra (orden_compra),
  KEY idx_pagos_estado (estado_transaccion)
) ENGINE=InnoDB;

CREATE TABLE pago_cotizaciones (
  pago_id       BIGINT UNSIGNED NOT NULL,
  cotizacion_id BIGINT UNSIGNED NOT NULL,

  PRIMARY KEY (pago_id, cotizacion_id),
  KEY ix_pago_cotizaciones_cotizacion (cotizacion_id),
  CONSTRAINT fk_pago_carrito_pago
    FOREIGN KEY (pago_id) REFERENCES pagos(id),
  CONSTRAINT fk_pago_carrito_cotizacion
    FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones(id)
) ENGINE=InnoDB;

CREATE TABLE pedidos (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  cotizacion_id    BIGINT UNSIGNED NOT NULL,
  cliente_id       BIGINT UNSIGNED NOT NULL,
  agenda_bloque_id BIGINT UNSIGNED NULL,
  vendedor_id      BIGINT UNSIGNED NULL,
  estado           VARCHAR(20) NOT NULL DEFAULT 'pagado',
  visto            TINYINT(1) NOT NULL DEFAULT 1,
  creado_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_pedidos_cotizacion
    FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones(id),
  CONSTRAINT fk_pedidos_cliente
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
  CONSTRAINT fk_pedidos_agenda
    FOREIGN KEY (agenda_bloque_id) REFERENCES agenda_bloques(id),
  CONSTRAINT fk_pedidos_vendedor
    FOREIGN KEY (vendedor_id) REFERENCES usuarios(id),
  UNIQUE KEY uq_pedidos_cotizacion (cotizacion_id),
  KEY idx_pedidos_estado (estado),
  KEY idx_pedidos_cliente (cliente_id)
) ENGINE=InnoDB;


-- Comisiones
CREATE TABLE comisiones (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  pedido_id   BIGINT UNSIGNED NOT NULL,
  usuario_id  BIGINT UNSIGNED NOT NULL,
  porcentaje  DECIMAL(5,2) NULL,
  monto       DECIMAL(12,2) NOT NULL,
  estado      VARCHAR(20) NOT NULL DEFAULT 'liquidable',
  creada_en   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  pagada_en   DATETIME NULL,

  CONSTRAINT fk_comision_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
  CONSTRAINT fk_comision_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT uq_comision_pedido_usuario
    UNIQUE (pedido_id, usuario_id),
  CONSTRAINT chk_comision_porcentaje
    CHECK (porcentaje IS NULL OR (porcentaje > 0 AND porcentaje <= 100)),
  CONSTRAINT chk_comision_monto
    CHECK (monto >= 0),
  CONSTRAINT chk_comision_estado
    CHECK (estado IN ('liquidable', 'pagada')),
  KEY idx_comisiones_estado (estado),
  KEY idx_comisiones_usuario (usuario_id)
) ENGINE=InnoDB;


-- Resenas y postventa
CREATE TABLE resenas (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  usuario_id    BIGINT UNSIGNED NULL,
  nombre        VARCHAR(150) NOT NULL,
  descripcion   TEXT NOT NULL,
  calidad       INT UNSIGNED NOT NULL,
  servicio      INT UNSIGNED NOT NULL,
  fecha         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editada       TINYINT(1) NOT NULL DEFAULT 0,
  fecha_edicion DATETIME NULL,

  CONSTRAINT fk_resenas_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT chk_resenas_calidad
    CHECK (calidad BETWEEN 1 AND 5),
  CONSTRAINT chk_resenas_servicio
    CHECK (servicio BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE postventa_solicitudes (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  usuario_id  BIGINT UNSIGNED NULL,
  nombre      VARCHAR(100) NOT NULL,
  apellido    VARCHAR(100) NOT NULL,
  correo      VARCHAR(150) NOT NULL,
  telefono    VARCHAR(20) NOT NULL,
  comuna      VARCHAR(100) NOT NULL,
  descripcion TEXT NOT NULL,
  estado      VARCHAR(20) NOT NULL DEFAULT 'pendiente',
  fecha       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_postventa_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;


-- Comunicaciones
CREATE TABLE correos_pendientes (
  id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  destinatario       VARCHAR(150) NOT NULL,
  asunto             VARCHAR(200) NOT NULL,
  contenido          TEXT NOT NULL,
  estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  intentos           INT NOT NULL DEFAULT 0,
  proximo_intento_en DATETIME NOT NULL,
  creado_en          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  enviado_en         DATETIME NULL,

  KEY idx_correos_pendientes (estado, proximo_intento_en)
) ENGINE=InnoDB;


-- Facturacion
CREATE TABLE facturacion_campos (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  clave       VARCHAR(50) NOT NULL UNIQUE,
  etiqueta    VARCHAR(100) NOT NULL,
  tipo        VARCHAR(10) NOT NULL,
  obligatorio TINYINT(1) NOT NULL DEFAULT 0,
  activo      TINYINT(1) NOT NULL DEFAULT 1,

  CONSTRAINT chk_facturacion_tipo
    CHECK (tipo IN ('TEXTO', 'EMAIL', 'NUMERO'))
) ENGINE=InnoDB;

CREATE TABLE facturacion_valores (
  cliente_id BIGINT UNSIGNED NOT NULL,
  campo_id   BIGINT UNSIGNED NOT NULL,
  valor      VARCHAR(500) NOT NULL,

  PRIMARY KEY (cliente_id, campo_id),
  CONSTRAINT fk_facturacion_cliente
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  CONSTRAINT fk_facturacion_campo
    FOREIGN KEY (campo_id) REFERENCES facturacion_campos(id)
) ENGINE=InnoDB;


-- Reglas automaticas de base de datos
DELIMITER $$

CREATE TRIGGER trg_pedidos_valida_confeccion
BEFORE UPDATE ON pedidos
FOR EACH ROW
BEGIN
  IF NEW.estado = 'en_confeccion' AND OLD.estado != 'pagado' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'RN-04: un pedido solo puede pasar a en_confeccion desde el estado pagado.';
  END IF;
END$$

CREATE EVENT ev_liberar_bloques_vencidos
ON SCHEDULE EVERY 1 MINUTE
DO
BEGIN
  UPDATE agenda_bloques
  SET estado = 'disponible',
      cotizacion_id = NULL,
      reservado_hasta = NULL
  WHERE estado = 'pre_reservado'
    AND reservado_hasta IS NOT NULL
    AND reservado_hasta < NOW();
END$$

DELIMITER ;
