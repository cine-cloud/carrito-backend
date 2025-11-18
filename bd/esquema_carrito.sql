-- Script compatible con PostgreSQL
BEGIN;

-- Tabla CARRITOS (carritos de compra)
CREATE TABLE IF NOT EXISTS carritos (
  carrito_id VARCHAR(36) PRIMARY KEY,
  usuario_id VARCHAR(255) NOT NULL,
  estado VARCHAR(32) NOT NULL,
  total NUMERIC(12,2) NOT NULL DEFAULT 0
);

-- Tabla ITEMS DEL CARRITO
CREATE TABLE IF NOT EXISTS carrito_items (
  item_id VARCHAR(36) PRIMARY KEY,
  carrito_id VARCHAR(36) NOT NULL REFERENCES carritos(carrito_id) ON DELETE CASCADE,
  pelicula_id INTEGER NOT NULL,
  titulo VARCHAR(255) NOT NULL,
  precio_unitario NUMERIC(12,2) NOT NULL,
  sinopsis TEXT,
  imagen_ampliada VARCHAR(255),
  condicion VARCHAR(50) NOT NULL,
  formato VARCHAR(50) NOT NULL,
  cantidad INTEGER NOT NULL
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_carrito_items_carrito ON carrito_items (carrito_id);
CREATE INDEX IF NOT EXISTS idx_carrito_items_pelicula ON carrito_items (pelicula_id);

COMMIT;
