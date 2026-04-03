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
  ('BLS', 'bls'),
  ('HH', 'hh')
ON CONFLICT DO NOTHING;

INSERT INTO subpresupuestos (cod_presupuesto, cod_subpresupuesto, descripcion) VALUES
  ('0401001', '01', 'Obras preliminares')
ON CONFLICT DO NOTHING;

INSERT INTO partidas (cod_partida, descripcion, cod_unidad, rendimiento_mo, rendimiento_eq) VALUES
  ('010101', 'Excavación manual en terreno normal', 'M3', 1.20, 0.00)
ON CONFLICT DO NOTHING;

INSERT INTO presupuesto_partida (cod_presupuesto, cod_subpresupuesto, cod_partida, precio1, horas_hombre, horas_maquina, ano, mes) VALUES
  ('0401001', '01', '010101', 125.50, 0.80, 0.00, 2026, 4);

INSERT INTO insumos (cod_insumo, descripcion, cod_unidad) VALUES
  ('020101', 'Cemento Portland Tipo I', 'BLS'),
  ('030201', 'Peón', 'HH')
ON CONFLICT DO NOTHING;

INSERT INTO partida_detalle (cod_partida, cod_insumo, tipo, cuadrilla, cantidad) VALUES
  ('010101', '020101', 1, 1.0, 3.5),
  ('010101', '030201', 2, 1.0, 1.2);

INSERT INTO precio_particular_insumo (cod_presupuesto, cod_subpresupuesto, cod_insumo, precio1, ano, mes) VALUES
  ('0401001', '01', '020101', 32.75, 2026, 4),
  ('0401001', '01', '030201', 6.50, 2026, 4);
