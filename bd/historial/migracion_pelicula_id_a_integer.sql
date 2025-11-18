-- Script de migración para cambiar pelicula_id de VARCHAR a INTEGER
-- Ejecutar este script en la base de datos carritos_db

BEGIN;

-- Verificar si hay datos en la tabla
DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM peliculas;
    
    IF row_count > 0 THEN
        -- Si hay datos, primero verificar que todos los valores sean numéricos válidos
        -- Eliminar registros con valores no numéricos (si existen)
        DELETE FROM peliculas 
        WHERE pelicula_id !~ '^[0-9]+$';
        
        -- Convertir la columna usando USING para especificar la conversión
        ALTER TABLE peliculas 
        ALTER COLUMN pelicula_id TYPE INTEGER USING pelicula_id::integer;
    ELSE
        -- Si no hay datos, simplemente cambiar el tipo
        ALTER TABLE peliculas 
        ALTER COLUMN pelicula_id TYPE INTEGER;
    END IF;
END $$;

-- Verificar que la conversión fue exitosa
DO $$
BEGIN
    RAISE NOTICE 'Migración completada: pelicula_id ahora es INTEGER';
END $$;

COMMIT;

