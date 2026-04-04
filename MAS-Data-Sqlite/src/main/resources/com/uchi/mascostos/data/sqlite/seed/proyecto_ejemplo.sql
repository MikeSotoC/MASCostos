PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS proyectos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  codigo TEXT NOT NULL UNIQUE,
  nombre TEXT NOT NULL,
  cliente TEXT,
  ubicacion TEXT,
  moneda TEXT NOT NULL DEFAULT 'PEN',
  estado TEXT NOT NULL DEFAULT 'ACTIVO',
  observaciones TEXT
);

CREATE TABLE IF NOT EXISTS proyecto_subpresupuestos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  proyecto_id INTEGER NOT NULL,
  cod_subpresupuesto TEXT,
  nombre TEXT NOT NULL,
  orden INTEGER NOT NULL,
  activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS proyecto_partidas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  proyecto_id INTEGER NOT NULL,
  subpresupuesto_id INTEGER,
  cod_partida_base TEXT,
  descripcion TEXT NOT NULL,
  unidad TEXT,
  metrado REAL NOT NULL DEFAULT 0,
  precio_unitario REAL NOT NULL DEFAULT 0,
  parcial REAL NOT NULL DEFAULT 0,
  rendimiento_mo REAL,
  rendimiento_eq REAL,
  horas_hombre REAL,
  horas_maquina REAL,
  origen TEXT NOT NULL DEFAULT 'BASE',
  orden INTEGER NOT NULL,
  activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS proyecto_partida_detalle (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  proyecto_partida_id INTEGER NOT NULL,
  cod_insumo_base TEXT,
  descripcion TEXT NOT NULL,
  unidad TEXT,
  tipo INTEGER,
  cuadrilla REAL,
  cantidad REAL NOT NULL,
  precio_unitario REAL NOT NULL,
  parcial REAL NOT NULL,
  origen TEXT NOT NULL DEFAULT 'BASE',
  activo INTEGER NOT NULL DEFAULT 1
);

INSERT INTO proyectos (codigo, nombre, cliente, ubicacion, moneda, estado, observaciones)
VALUES ('PRJ-DEMO-001', 'Proyecto Demo MASCostos', 'Cliente Demo SAC', 'Lima', 'PEN', 'ACTIVO', 'Proyecto semilla de ejemplo')
ON CONFLICT(codigo) DO NOTHING;

INSERT INTO proyecto_subpresupuestos (proyecto_id, cod_subpresupuesto, nombre, orden, activo)
SELECT id, '01', 'Obras preliminares', 1, 1
FROM proyectos
WHERE codigo = 'PRJ-DEMO-001';

INSERT INTO proyecto_subpresupuestos (proyecto_id, cod_subpresupuesto, nombre, orden, activo)
SELECT id, '03', 'Concreto simple y armado', 2, 1
FROM proyectos
WHERE codigo = 'PRJ-DEMO-001';

INSERT INTO proyecto_partidas (
  proyecto_id, subpresupuesto_id, cod_partida_base, descripcion, unidad,
  metrado, precio_unitario, parcial, rendimiento_mo, rendimiento_eq,
  horas_hombre, horas_maquina, origen, orden, activo
)
SELECT p.id, s.id, '010101', '010101', NULL,
       20.0, 125.50, 2510.0, 1.20, 0.00,
       0.80, 0.00, 'BASE', 1, 1
FROM proyectos p
JOIN proyecto_subpresupuestos s ON s.proyecto_id = p.id
WHERE p.codigo = 'PRJ-DEMO-001' AND s.cod_subpresupuesto = '01';

INSERT INTO proyecto_partidas (
  proyecto_id, subpresupuesto_id, cod_partida_base, descripcion, unidad,
  metrado, precio_unitario, parcial, rendimiento_mo, rendimiento_eq,
  horas_hombre, horas_maquina, origen, orden, activo
)
SELECT p.id, s.id, '030101', '030101', NULL,
       15.0, 410.25, 6153.75, 1.10, 0.15,
       1.10, 0.15, 'BASE', 1, 1
FROM proyectos p
JOIN proyecto_subpresupuestos s ON s.proyecto_id = p.id
WHERE p.codigo = 'PRJ-DEMO-001' AND s.cod_subpresupuesto = '03';

INSERT INTO proyecto_partida_detalle (
  proyecto_partida_id, cod_insumo_base, descripcion, unidad,
  tipo, cuadrilla, cantidad, precio_unitario, parcial, origen, activo
)
SELECT pp.id, '030201', '030201', NULL, 2, 1.0, 1.2, 6.5, 7.8, 'BASE', 1
FROM proyecto_partidas pp
JOIN proyectos p ON p.id = pp.proyecto_id
WHERE p.codigo = 'PRJ-DEMO-001' AND pp.cod_partida_base = '010101';

INSERT INTO proyecto_partida_detalle (
  proyecto_partida_id, cod_insumo_base, descripcion, unidad,
  tipo, cuadrilla, cantidad, precio_unitario, parcial, origen, activo
)
SELECT pp.id, '020101', '020101', NULL, 1, 1.0, 7.0, 32.75, 229.25, 'BASE', 1
FROM proyecto_partidas pp
JOIN proyectos p ON p.id = pp.proyecto_id
WHERE p.codigo = 'PRJ-DEMO-001' AND pp.cod_partida_base = '030101';
