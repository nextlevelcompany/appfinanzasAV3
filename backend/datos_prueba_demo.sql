-- Datos de demostración para Insomnia/Adminer.
-- Se insertan solo si aún no existe la misma clave de demostración.
BEGIN;

INSERT INTO cuentas (nombre, tipo, saldo, moneda)
SELECT 'DEMO - Ahorros', 'AHORROS', 5000, 'PEN'
WHERE NOT EXISTS (SELECT 1 FROM cuentas WHERE nombre = 'DEMO - Ahorros');

INSERT INTO cuentas (nombre, tipo, saldo, moneda)
SELECT 'DEMO - Efectivo', 'EFECTIVO', 350, 'PEN'
WHERE NOT EXISTS (SELECT 1 FROM cuentas WHERE nombre = 'DEMO - Efectivo');

INSERT INTO categorias (nombre, tipo, presupuesto_limite, color_hex)
SELECT 'DEMO - Sueldo', 'INGRESO', 0, '#16803C'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'DEMO - Sueldo');

INSERT INTO categorias (nombre, tipo, presupuesto_limite, color_hex)
SELECT 'DEMO - Alimentación', 'GASTO', 900, '#D97706'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'DEMO - Alimentación');

INSERT INTO categorias (nombre, tipo, presupuesto_limite, color_hex)
SELECT 'DEMO - Transporte', 'GASTO', 250, '#2563EB'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'DEMO - Transporte');

INSERT INTO transacciones (descripcion, monto, tipo, categoria, fecha)
SELECT 'DEMO - Sueldo octubre', 3500, 'INGRESO', 'DEMO - Sueldo', '2026-10-01'
WHERE NOT EXISTS (SELECT 1 FROM transacciones WHERE descripcion = 'DEMO - Sueldo octubre' AND fecha = '2026-10-01');

INSERT INTO transacciones (descripcion, monto, tipo, categoria, fecha)
SELECT 'DEMO - Mercado octubre', 420, 'GASTO', 'DEMO - Alimentación', '2026-10-03'
WHERE NOT EXISTS (SELECT 1 FROM transacciones WHERE descripcion = 'DEMO - Mercado octubre' AND fecha = '2026-10-03');

INSERT INTO transacciones (descripcion, monto, tipo, categoria, fecha)
SELECT 'DEMO - Transporte octubre', 120, 'GASTO', 'DEMO - Transporte', '2026-10-04'
WHERE NOT EXISTS (SELECT 1 FROM transacciones WHERE descripcion = 'DEMO - Transporte octubre' AND fecha = '2026-10-04');

INSERT INTO transacciones (descripcion, monto, tipo, categoria, fecha)
SELECT 'DEMO - Trabajo freelance', 800, 'INGRESO', 'DEMO - Sueldo', '2026-10-07'
WHERE NOT EXISTS (SELECT 1 FROM transacciones WHERE descripcion = 'DEMO - Trabajo freelance' AND fecha = '2026-10-07');

COMMIT;
