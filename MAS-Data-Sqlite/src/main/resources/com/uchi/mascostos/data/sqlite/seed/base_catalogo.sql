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
  ('HH', 'hh'),
  ('HM', 'hm')
ON CONFLICT DO NOTHING;

INSERT INTO subpresupuestos (cod_presupuesto, cod_subpresupuesto, descripcion) VALUES
  ('0401001', '01', 'Obras preliminares'),
  ('0401001', '02', 'Movimiento de tierras'),
  ('0401001', '03', 'Concreto simple y armado'),
  ('0401001', '04', 'Encofrado y acero')
ON CONFLICT DO NOTHING;

INSERT INTO partidas (cod_partida, descripcion, cod_unidad, rendimiento_mo, rendimiento_eq) VALUES
  ('010101', 'Excavación manual en terreno normal', 'M3', 1.20, 0.00),
  ('010102', 'Trazo y replanteo en terreno', 'M2', 0.40, 0.00),
  ('020201', 'Relleno y compactado con material propio', 'M3', 0.65, 0.20),
  ('030101', 'Concreto f''c=210 kg/cm2 para zapatas', 'M3', 1.10, 0.15),
  ('040101', 'Acero de refuerzo fy=4200 kg/cm2', 'KG', 0.30, 0.00)
ON CONFLICT DO NOTHING;

INSERT INTO presupuesto_partida (cod_presupuesto, cod_subpresupuesto, cod_partida, precio1, horas_hombre, horas_maquina, ano, mes) VALUES
  ('0401001', '01', '010101', 125.50, 0.80, 0.00, 2026, 4),
  ('0401001', '01', '010102', 8.90, 0.25, 0.00, 2026, 4),
  ('0401001', '02', '020201', 56.40, 0.45, 0.15, 2026, 4),
  ('0401001', '03', '030101', 410.25, 1.10, 0.15, 2026, 4),
  ('0401001', '04', '040101', 6.80, 0.10, 0.00, 2026, 4);

INSERT INTO insumos (cod_insumo, descripcion, cod_unidad) VALUES
  ('020101', 'Cemento Portland Tipo I', 'BLS'),
  ('020102', 'Arena gruesa', 'M3'),
  ('020103', 'Piedra chancada 1/2"', 'M3'),
  ('030201', 'Peón', 'HH'),
  ('030202', 'Oficial', 'HH'),
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
  ('040101', '030202', 2, 1.0, 0.10);

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
  ('0401001', '04', '030202', 8.90, 2026, 4);
