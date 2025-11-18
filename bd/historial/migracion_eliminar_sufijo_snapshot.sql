-- Script de migración para eliminar el sufijo _snapshot de las columnas de carrito_items
-- Ejecutar este script en la base de datos existente

BEGIN;

-- Renombrar las columnas eliminando el sufijo _snapshot
ALTER TABLE carrito_items 
RENAME COLUMN titulo_snapshot TO titulo;

ALTER TABLE carrito_items 
RENAME COLUMN sinopsis_snapshot TO sinopsis;

ALTER TABLE carrito_items 
RENAME COLUMN imagen_ampliada_snapshot TO imagen_ampliada;

ALTER TABLE carrito_items 
RENAME COLUMN condicion_snapshot TO condicion;

ALTER TABLE carrito_items 
RENAME COLUMN formato_snapshot TO formato;

COMMIT;

