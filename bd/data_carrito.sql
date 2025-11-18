-- Plantilla SQL para insertar usuarios y un carrito con items en la BD de carritos
-- Reemplazar los tokens en mayÃºsculas por valores concretos antes de ejecutar.

BEGIN;

-- Crear tabla usuarios si no existe
CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(255)
);

-- Insertar dos usuarios
INSERT INTO
    usuarios (usuario_id, nombre)
VALUES ('user1', 'Usuario Test 1')
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO
    usuarios (usuario_id, nombre)
VALUES ('user2', 'Usuario Test 2')
ON CONFLICT (usuario_id) DO NOTHING;

-- Crear carrito
INSERT INTO
    carritos (
        carrito_id,
        usuario_id,
        estado,
        total
    )
VALUES (
        '373b5036-ae16-4ade-8a6e-835a2ff273e1',
        'user1',
        'ABIERTO',
        0
    )
ON CONFLICT (carrito_id) DO NOTHING;

-- ITEMS: este bloque serÃ¡ reemplazado por la lista concreta de INSERTs
INSERT INTO carrito_items (item_id, carrito_id, pelicula_id, titulo, precio_unitario, sinopsis, imagen_ampliada, condicion, formato, cantidad) 
VALUES ('bbe48423-f536-4346-aaee-9f511963c62a', '373b5036-ae16-4ade-8a6e-835a2ff273e1', 57, 'Inception', 3500.00, 
        'Un ladrón que roba secretos corporativos a través del uso de la tecnología de sueños compartidos tiene la tarea inversa de plantar una idea en la mente de un CEO.',
        'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg',
        'Nuevo', 'Blu-ray', 1) 
ON CONFLICT (item_id) DO NOTHING;

INSERT INTO carrito_items (item_id, carrito_id, pelicula_id, titulo, precio_unitario, sinopsis, imagen_ampliada, condicion, formato, cantidad) 
VALUES ('e0b76cfd-dde9-4661-b778-be084d16eb98', '373b5036-ae16-4ade-8a6e-835a2ff273e1', 58, 'The Dark Knight', 1250.00,
        'Batman se enfrenta al Joker, un criminal que siembra el caos en Gotham City.',
        'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg',
        'Usado', 'DVD', 1) 
ON CONFLICT (item_id) DO NOTHING;

-- Actualizar total
UPDATE carritos c
SET
    total = COALESCE(
        (
            SELECT SUM(
                    ci.precio_unitario * ci.cantidad
                )
            FROM carrito_items ci
            WHERE
                ci.carrito_id = c.carrito_id
        ),
        0
    )
WHERE
    c.carrito_id = '373b5036-ae16-4ade-8a6e-835a2ff273e1';

COMMIT;
