-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS carrito_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE carrito_db;

-- Tabla CARRITOS (carritos de compra)
CREATE TABLE IF NOT EXISTS carritos (
  carrito_id VARCHAR(36) PRIMARY KEY,
  usuario_id VARCHAR(100) NOT NULL,
  estado VARCHAR(30) NOT NULL,
  total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla ITEMS DEL CARRITO
CREATE TABLE IF NOT EXISTS carrito_items (
  item_id VARCHAR(36) PRIMARY KEY,
  carrito_id VARCHAR(36) NOT NULL,
  pelicula_id INT NOT NULL,
  titulo_snapshot VARCHAR(255) NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  cantidad INT NOT NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_carrito_item_carrito FOREIGN KEY (carrito_id) REFERENCES carritos(carrito_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);

-- Índices para mejorar el rendimiento (MySQL no soporta IF NOT EXISTS para índices)
-- Si ya existen, se ignorarán
CREATE INDEX IF NOT EXISTS idx_carrito_items_carrito ON carrito_items(carrito_id);
CREATE INDEX IF NOT EXISTS idx_carrito_items_pelicula ON carrito_items(pelicula_id);
