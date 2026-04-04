PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS subpresupuestos (
  cod_presupuesto TEXT NOT NULL,
  cod_subpresupuesto TEXT NOT NULL,
  descripcion TEXT NOT NULL,
  PRIMARY KEY (cod_presupuesto, cod_subpresupuesto)
);

CREATE TABLE IF NOT EXISTS unidades (
  cod_unidad TEXT PRIMARY KEY,
  simbolo TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS partidas (
  cod_partida TEXT PRIMARY KEY,
  descripcion TEXT,
  cod_unidad TEXT,
  rendimiento_mo REAL,
  rendimiento_eq REAL
);

CREATE TABLE IF NOT EXISTS presupuesto_partida (
  cod_presupuesto TEXT NOT NULL,
  cod_subpresupuesto TEXT NOT NULL,
  cod_partida TEXT NOT NULL,
  precio1 REAL,
  horas_hombre REAL,
  horas_maquina REAL,
  ano INTEGER DEFAULT 2026,
  mes INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS insumos (
  cod_insumo TEXT PRIMARY KEY,
  descripcion TEXT,
  cod_unidad TEXT
);

CREATE TABLE IF NOT EXISTS partida_detalle (
  id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
  cod_partida TEXT NOT NULL,
  cod_insumo TEXT,
  tipo INTEGER,
  cuadrilla REAL,
  cantidad REAL
);

CREATE TABLE IF NOT EXISTS precio_particular_insumo (
  cod_presupuesto TEXT NOT NULL,
  cod_subpresupuesto TEXT NOT NULL,
  cod_insumo TEXT NOT NULL,
  precio1 REAL,
  ano INTEGER,
  mes INTEGER
);

INSERT INTO unidades (cod_unidad, simbolo) VALUES
  ('M3', 'm3'),
  ('M2', 'm2'),
  ('ML', 'ml'),
  ('BLS', 'bls'),
  ('KG', 'kg'),
  ('UND', 'und'),
  ('GL', 'gl'),
  ('HH', 'hh'),
  ('HM', 'hm')
ON CONFLICT DO NOTHING;

INSERT INTO subpresupuestos (cod_presupuesto, cod_subpresupuesto, descripcion) VALUES
  ('0401001', '01', 'Obras preliminares'),
  ('0401001', '02', 'Movimiento de tierras'),
  ('0401001', '03', 'Concreto simple y armado'),
  ('0401001', '04', 'Encofrado y acero'),
  ('0401001', '05', 'Albañilería y tarrajeo'),
  ('0401001', '06', 'Instalaciones sanitarias'),
  ('0401001', '07', 'Instalaciones eléctricas'),
  ('0401001', '08', 'Acabados')
ON CONFLICT DO NOTHING;

INSERT INTO partidas (cod_partida, descripcion, cod_unidad, rendimiento_mo, rendimiento_eq) VALUES
  ('010101', 'Excavación manual en terreno normal', 'M3', 1.20, 0.00),
  ('010102', 'Trazo y replanteo en terreno', 'M2', 0.40, 0.00),
  ('020201', 'Relleno y compactado con material propio', 'M3', 0.65, 0.20),
  ('030101', 'Concreto f''c=210 kg/cm2 para zapatas', 'M3', 1.10, 0.15),
  ('040101', 'Acero de refuerzo fy=4200 kg/cm2', 'KG', 0.30, 0.00),
  ('050101', 'Muro de ladrillo king kong 18 huecos', 'M2', 0.90, 0.00),
  ('050201', 'Tarrajeo de muros interiores', 'M2', 0.55, 0.00),
  ('060101', 'Tubería PVC SAP 1/2"', 'ML', 0.25, 0.00),
  ('060201', 'Instalación de inodoro one piece', 'UND', 0.80, 0.00),
  ('070101', 'Cable THW-90 2.5 mm2', 'ML', 0.20, 0.00),
  ('070201', 'Tomacorriente doble + placa', 'UND', 0.35, 0.00),
  ('080101', 'Piso porcelanato 60x60', 'M2', 0.45, 0.00),
  ('080201', 'Pintura látex en muros interiores', 'M2', 0.35, 0.00)
ON CONFLICT DO NOTHING;

INSERT INTO presupuesto_partida (cod_presupuesto, cod_subpresupuesto, cod_partida, precio1, horas_hombre, horas_maquina, ano, mes) VALUES
  ('0401001', '01', '010101', 125.50, 0.80, 0.00, 2026, 4),
  ('0401001', '01', '010102', 8.90, 0.25, 0.00, 2026, 4),
  ('0401001', '02', '020201', 56.40, 0.45, 0.15, 2026, 4),
  ('0401001', '03', '030101', 410.25, 1.10, 0.15, 2026, 4),
  ('0401001', '04', '040101', 6.80, 0.10, 0.00, 2026, 4),
  ('0401001', '05', '050101', 82.30, 0.90, 0.00, 2026, 4),
  ('0401001', '05', '050201', 26.40, 0.55, 0.00, 2026, 4),
  ('0401001', '06', '060101', 18.90, 0.25, 0.00, 2026, 4),
  ('0401001', '06', '060201', 520.00, 0.80, 0.00, 2026, 4),
  ('0401001', '07', '070101', 6.20, 0.20, 0.00, 2026, 4),
  ('0401001', '07', '070201', 42.50, 0.35, 0.00, 2026, 4),
  ('0401001', '08', '080101', 118.00, 0.45, 0.00, 2026, 4),
  ('0401001', '08', '080201', 16.80, 0.35, 0.00, 2026, 4);

INSERT INTO insumos (cod_insumo, descripcion, cod_unidad) VALUES
  ('020101', 'Cemento Portland Tipo I', 'BLS'),
  ('020102', 'Arena gruesa', 'M3'),
  ('020103', 'Piedra chancada 1/2"', 'M3'),
  ('020201', 'Ladrillo King Kong 18 huecos', 'UND'),
  ('020301', 'Tubería PVC SAP 1/2"', 'ML'),
  ('020401', 'Cable THW-90 2.5 mm2', 'ML'),
  ('020501', 'Porcelanato 60x60', 'M2'),
  ('020502', 'Pintura látex lavable', 'GL'),
  ('030201', 'Peón', 'HH'),
  ('030202', 'Oficial', 'HH'),
  ('030203', 'Operario', 'HH'),
  ('040301', 'Vibradora de concreto', 'HM'),
  ('050101', 'Acero corrugado fy=4200', 'KG')
ON CONFLICT DO NOTHING;

INSERT INTO partida_detalle (cod_partida, cod_insumo, tipo, cuadrilla, cantidad) VALUES
  ('010101', '030201', 2, 1.0, 1.20),
  ('010102', '030202', 2, 1.0, 0.25),
  ('020201', '030201', 2, 1.0, 0.45),
  ('020201', '040301', 3, 1.0, 0.15),
  ('030101', '020101', 1, 1.0, 7.00),
  ('030101', '020102', 1, 1.0, 0.45),
  ('030101', '020103', 1, 1.0, 0.80),
  ('030101', '030202', 2, 1.0, 0.60),
  ('040101', '050101', 1, 1.0, 1.00),
  ('040101', '030202', 2, 1.0, 0.10),
  ('050101', '020201', 1, 1.0, 48.00),
  ('050101', '030202', 2, 1.0, 0.70),
  ('050201', '020101', 1, 1.0, 0.20),
  ('050201', '030201', 2, 1.0, 0.45),
  ('060101', '020301', 1, 1.0, 1.00),
  ('060101', '030203', 2, 1.0, 0.20),
  ('070101', '020401', 1, 1.0, 1.00),
  ('070101', '030203', 2, 1.0, 0.15),
  ('080101', '020501', 1, 1.0, 1.05),
  ('080101', '030202', 2, 1.0, 0.30),
  ('080201', '020502', 1, 1.0, 0.08),
  ('080201', '030201', 2, 1.0, 0.25);

INSERT INTO precio_particular_insumo (cod_presupuesto, cod_subpresupuesto, cod_insumo, precio1, ano, mes) VALUES
  ('0401001', '01', '030201', 6.50, 2026, 4),
  ('0401001', '01', '030202', 8.75, 2026, 4),
  ('0401001', '02', '030201', 6.60, 2026, 4),
  ('0401001', '02', '040301', 45.00, 2026, 4),
  ('0401001', '03', '020101', 32.75, 2026, 4),
  ('0401001', '03', '020102', 52.20, 2026, 4),
  ('0401001', '03', '020103', 64.40, 2026, 4),
  ('0401001', '03', '030202', 8.90, 2026, 4),
  ('0401001', '04', '050101', 4.85, 2026, 4),
  ('0401001', '04', '030202', 8.90, 2026, 4),
  ('0401001', '05', '020201', 0.95, 2026, 4),
  ('0401001', '05', '020101', 32.75, 2026, 4),
  ('0401001', '05', '030201', 6.60, 2026, 4),
  ('0401001', '06', '020301', 9.80, 2026, 4),
  ('0401001', '06', '030203', 11.20, 2026, 4),
  ('0401001', '07', '020401', 1.45, 2026, 4),
  ('0401001', '07', '030203', 11.20, 2026, 4),
  ('0401001', '08', '020501', 78.00, 2026, 4),
  ('0401001', '08', '020502', 78.50, 2026, 4),
  ('0401001', '08', '030202', 8.90, 2026, 4);
