USE rollerapp;








INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, rol, activo) VALUES
('Paula', 'Reyes', 'admin.prueba@rollerapp.test', '$2a$10$qHQNLXPxEtH8gj9EQJEXMOqvsosSrVtEbqADbqMLqJ1c9Y44f.c4C', 'admin', 1),
('Camila', 'Soto', 'ventas.prueba@rollerapp.test', '$2a$10$Gh7.CexmE03FVni5og.F7OrBI5Fn2Aa01GyHf6uRZRXbDxk6ySJsC', 'vendedor', 1),
('Nicolás', 'Rojas', 'tecnico.prueba@rollerapp.test', '$2a$10$7lGgEc4HtAEBqxupHG8KP.n/PZR8vBh1HdaIFYSvEyYshBLFaDVkO', 'tecnico', 1),
('Fernanda', 'Morales', 'cliente.prueba@rollerapp.test', '$2a$10$/2kc11WBOHRt4d1Qoua/TelkRQgREVi7dVpCF9aWEAMcG8jdvciuu', 'cliente', 1);

INSERT INTO telas (nombre, descripcion, precio_m2) VALUES
('Blackout Estándar', 'Bloqueo total de luz', 18000),
('Screen 5%', 'Deja pasar algo de luz, protege de rayos UV', 15000),
('Lino Natural', 'Tela clásica, translúcida', 12000),
('Blackout Premium Térmico', 'Bloqueo de luz con aislante térmico y acústico', 22000),
('Screen 1%', 'Mayor privacidad y baja visibilidad hacia el interior', 17500),
('Screen 10%', 'Alta visibilidad hacia el exterior y excelente claridad', 14000),
('Dúo / Zebra', 'Tela de doble franja para control de luz alternado', 24000),
('Shangrilá Sheer', 'Dos velos translúcidos con lamas textiles horizontales regulables', 29000),
('Screen Exterior 5%', 'Tela técnica resistente para protección solar exterior', 23000),
('Vertical Translúcida', 'Tela para lamas verticales con filtrado suave de luz', 17000),
('Celular Honeycomb', 'Tejido plegado tipo panal con aislación térmica', 30000);

INSERT INTO mecanismos (nombre, descripcion, valor_fijo) VALUES
('Mecanismo Manual con Cadena', 'Sistema manual estándar', 12000),
('Motorizado con Control Remoto', 'Motor eléctrico + control', 65000),
('Motorizado Smart Wi-Fi', 'Compatible con Alexa, Google Home y control por App', 85000),
('Mecanismo con Cadena de Acero Inoxidable', 'Cadena metálica de alta resistencia y estética moderna', 16000),
('Sistema Doble Roller', 'Soporte para instalar dos cortinas en un mismo espacio', 28000),
('Sistema Shangrilá', 'Cabezal y control de inclinación para lamas textiles Shangrilá', 32000),
('Sistema Exterior con Manivela', 'Mecanismo reforzado para roller exterior', 35000),
('Sistema Vertical', 'Riel superior con giro y desplazamiento de lamas verticales', 30000),
('Sistema Celular Cordless', 'Sistema sin cadena para cortina celular tipo honeycomb', 34000),
('Sistema Roller con Rieles Laterales', 'Cassette y guías laterales para mantener la tela contenida', 39000);

INSERT INTO servicios (nombre, descripcion, precio) VALUES
('Instalación a domicilio', 'Visita técnica de instalación', 15000),
('Rectificación de medidas', 'Visita técnica para volver a medir', 8000),
('Mantención', 'Revisión y ajuste del mecanismo', 10000),
('Lavado profesional', 'Lavado de la tela en taller', 12000);

INSERT INTO productos (nombre, descripcion, categoria, imagen_principal, tela_defecto_id, mecanismo_defecto_id) VALUES
('Cortina Roller Blackout', 'Bloqueo total de luz, ideal para dormitorios.', 'blackout', 'roller-blackout.svg', 1, 1),
('Cortina Roller Screen', 'Filtra la luz solar sin oscurecer del todo.', 'moderna', 'roller-screen.svg', 2, 1),
('Cortina Roller Lino', 'Estilo clásico y liviano.', 'clasica', 'roller-lino.svg', 3, 1);




INSERT INTO productos (nombre, descripcion, categoria, imagen_principal, tela_defecto_id, mecanismo_defecto_id)
SELECT p.nombre, p.descripcion, p.categoria, p.imagen_principal, p.tela_defecto_id, p.mecanismo_defecto_id
FROM (
SELECT 'Cortina Roller Duo' AS nombre, 'Franjas alternadas para graduar luz y privacidad; requiere tela Duo / Zebra.' AS descripcion, 'moderna' AS categoria, 'roller-duo.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Dúo / Zebra' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Mecanismo Manual con Cadena' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Cortina Roller Shangrilá' AS nombre, 'Lamas textiles suaves suspendidas entre velos para regular luz, vista y privacidad.' AS descripcion, 'moderna' AS categoria, 'roller-shangrila.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Shangrilá Sheer' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Sistema Shangrilá' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Cortina Roller Natural Look' AS nombre, 'Apariencia textil natural; configuración referencial con Lino Natural.' AS descripcion, 'clasica' AS categoria, 'roller-natural.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Lino Natural' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Mecanismo Manual con Cadena' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Roller Tradicional' AS nombre, 'Roller de interior versátil con Screen 5% para filtrar luz manteniendo luminosidad.' AS descripcion, 'clasica' AS categoria, 'roller-clasica.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Screen 5%' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Mecanismo Manual con Cadena' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Roller de Exterior' AS nombre, 'Roller reforzado para terraza o pérgola, diseñado para reducir sol y deslumbramiento exterior.' AS descripcion, 'moderna' AS categoria, 'roller-exterior.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Screen Exterior 5%' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Sistema Exterior con Manivela' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Cortina Vertical' AS nombre, 'Lamas verticales orientables para ventanales amplios y control gradual de luz.' AS descripcion, 'panel' AS categoria, 'roller-vertical.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Vertical Translúcida' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Sistema Vertical' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Cortinas Celular' AS nombre, 'Estructura tipo panal que filtra la luz y mejora la aislación térmica del ventanal.' AS descripcion, 'moderna' AS categoria, 'roller-celular.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Celular Honeycomb' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Sistema Celular Cordless' LIMIT 1) AS mecanismo_defecto_id
UNION ALL
SELECT 'Roller con Rieles Laterales' AS nombre, 'Blackout guiado por rieles laterales para reducir las filtraciones de luz en los bordes.' AS descripcion, 'moderna' AS categoria, 'roller-rieles.svg' AS imagen_principal, (SELECT id FROM telas WHERE nombre = 'Blackout Premium Térmico' LIMIT 1) AS tela_defecto_id, (SELECT id FROM mecanismos WHERE nombre = 'Sistema Roller con Rieles Laterales' LIMIT 1) AS mecanismo_defecto_id
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos existente WHERE existente.nombre = p.nombre);

INSERT INTO agenda_bloques (fecha, hora_inicio, hora_fin, tecnico_id, estado) VALUES
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:00:00', '11:00:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '12:00:00', '13:00:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '15:00:00', '16:00:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:30:00', '11:30:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '14:00:00', '15:00:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '16:00:00', '17:00:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 9 DAY), '09:30:00', '10:30:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible'),
(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '11:30:00', '12:30:00', (SELECT id FROM usuarios WHERE correo = 'tecnico.prueba@rollerapp.test'), 'disponible');


UPDATE telas SET paso_luz = 'OPACO' WHERE LOWER(nombre) LIKE '%blackout%';
UPDATE telas SET paso_luz = 'REGULABLE' WHERE LOWER(nombre) LIKE '%zebra%' OR LOWER(nombre) LIKE '%dúo%' OR LOWER(nombre) LIKE '%duo%';
UPDATE telas SET paso_luz = 'TRANSLUCIDO' WHERE LOWER(nombre) LIKE '%lino%' OR LOWER(nombre) LIKE '%sheer%';



INSERT INTO facturacion_campos(clave,etiqueta,tipo,obligatorio,activo) VALUES
('rut','RUT','TEXTO',1,1),('razon_social','Razón social','TEXTO',1,1),('giro','Giro comercial','TEXTO',0,1),('direccion','Dirección de facturación','TEXTO',1,1),('correo','Correo de facturación','EMAIL',1,1);

INSERT INTO facturacion_valores(cliente_id,campo_id,valor) SELECT u.id,c.id,CASE c.clave WHEN 'rut' THEN '12.345.678-5' WHEN 'razon_social' THEN 'Fernanda Morales' WHEN 'giro' THEN 'Servicios independientes' WHEN 'direccion' THEN 'Av. Grecia 7850, Peñalolén' WHEN 'correo' THEN 'cliente.prueba@rollerapp.test' END FROM usuarios u CROSS JOIN facturacion_campos c WHERE u.correo='cliente.prueba@rollerapp.test';

UPDATE servicios SET comision_tecnico = 5000 WHERE id = 1;

INSERT INTO cotizaciones(cliente_id,producto_id,tela_id,mecanismo_id,ancho_cm,alto_cm,valor_tela,valor_mecanismo,valor_servicios,direccion,comuna,estado) VALUES
(4,1,1,1,100,100,18000,12000,15000,'Av. Grecia 7850','Santiago','pagada'),
(4,2,2,1,100,150,22500,12000,15000,'Av. Grecia 7850','Santiago','pagada'),
(4,1,1,1,100,100,18000,12000,0,'Av. Grecia 7850','Santiago','pendiente_pago');

INSERT INTO cotizacion_servicios(cotizacion_id,servicio_id,precio_aplicado,comision_tecnico_aplicada) VALUES (1,1,15000,5000),(2,1,15000,5000);

INSERT INTO agenda_bloques(fecha,hora_inicio,hora_fin,tecnico_id,cotizacion_id,estado,atencion_inicio,atencion_fin) VALUES
(DATE_SUB(CURDATE(),INTERVAL 1 DAY),'09:00:00','10:00:00',3,1,'completado',TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL 1 DAY),'09:00:00'),TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL 1 DAY),'09:30:00')),
(CURDATE(),'14:00:00','15:00:00',3,2,'reservado',NULL,NULL);

INSERT INTO pedidos(cotizacion_id,cliente_id,agenda_bloque_id,vendedor_id,estado,visto) SELECT 1,4,id,2,'realizado',0 FROM agenda_bloques WHERE cotizacion_id=1;
INSERT INTO pedidos(cotizacion_id,cliente_id,agenda_bloque_id,vendedor_id,estado,visto) SELECT 2,4,id,2,'en_terreno',0 FROM agenda_bloques WHERE cotizacion_id=2;

INSERT INTO pagos(cotizacion_id,orden_compra,monto,estado_transaccion,codigo_autorizacion,tipo_pago,tarjeta_ultimos_digitos,fecha_transaccion) VALUES
(1,'EJEMPLO-VENTA-1',45000,'autorizada','EJEMPLO','VD','0000',NOW()),
(2,'EJEMPLO-VENTA-2',49500,'autorizada','EJEMPLO','VD','0000',NOW());
INSERT INTO pago_cotizaciones(pago_id,cotizacion_id) SELECT id,cotizacion_id FROM pagos;

INSERT INTO comisiones(pedido_id,usuario_id,porcentaje,monto,estado) VALUES(1,2,5,2250,'liquidable'),(1,3,NULL,5000,'liquidable');
INSERT INTO observaciones_visita(bloque_id,autor_id,texto) SELECT id,3,'Instalación terminada. Se revisó el funcionamiento de la cadena junto con la clienta.' FROM agenda_bloques WHERE cotizacion_id=1;
INSERT INTO resenas(usuario_id,nombre,descripcion,calidad,servicio) VALUES(4,'Fernanda Morales','Quedaron muy bien instaladas y el técnico explicó cómo cuidarlas. Todo salió dentro del horario acordado.',5,5);
INSERT INTO postventa_solicitudes(usuario_id,nombre,apellido,correo,telefono,comuna,descripcion) VALUES(4,'Fernanda','Morales','cliente.prueba@rollerapp.test','+56912345678','Santiago','La cadena quedó un poco tensa después de la instalación. Solicito revisión.');
