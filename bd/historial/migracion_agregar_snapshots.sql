-- Script de migración para agregar campos de snapshot a carrito_items
-- Ejecutar este script en la base de datos existente

BEGIN;

-- Agregar columnas de snapshot si no existen
ALTER TABLE carrito_items 
ADD COLUMN IF NOT EXISTS sinopsis_snapshot TEXT,
ADD COLUMN IF NOT EXISTS imagen_ampliada_snapshot VARCHAR(255),
ADD COLUMN IF NOT EXISTS condicion_snapshot VARCHAR(50),
ADD COLUMN IF NOT EXISTS formato_snapshot VARCHAR(50);

-- Actualizar valores existentes con datos de la tabla peliculas
-- (Esto sincroniza los items existentes con los datos actuales de películas)
UPDATE carrito_items ci
SET 
    sinopsis_snapshot = p.sinopsis,
    imagen_ampliada_snapshot = p.imagen_ampliada,
    condicion_snapshot = p.condicion,
    formato_snapshot = p.formato
FROM peliculas p
WHERE ci.pelicula_id = CAST(p.pelicula_id AS INTEGER)
AND (ci.sinopsis_snapshot IS NULL OR ci.condicion_snapshot IS NULL OR ci.formato_snapshot IS NULL);

-- Hacer las columnas NOT NULL después de actualizar los datos existentes
-- Primero actualizamos los valores NULL con valores por defecto si es necesario
UPDATE carrito_items 
SET condicion_snapshot = 'Nuevo' 
WHERE condicion_snapshot IS NULL;

UPDATE carrito_items 
SET formato_snapshot = 'DVD' 
WHERE formato_snapshot IS NULL;

-- Ahora podemos hacer las columnas NOT NULL
ALTER TABLE carrito_items 
ALTER COLUMN condicion_snapshot SET NOT NULL,
ALTER COLUMN formato_snapshot SET NOT NULL;

COMMIT;

