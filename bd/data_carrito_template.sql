-- Plantilla SQL para insertar usuarios y un carrito con items en la BD de carritos
-- Reemplazar los tokens en mayúsculas por valores concretos antes de ejecutar.

BEGIN;

-- Crear tabla usuarios si no existe
CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(255)
);

-- Insertar dos usuarios
INSERT INTO
    usuarios (usuario_id, nombre)
VALUES ('{{USER1}}', 'Usuario Test 1')
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO
    usuarios (usuario_id, nombre)
VALUES ('{{USER2}}', 'Usuario Test 2')
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
        '{{CART_ID}}',
        '{{USER1}}',
        'OPEN',
        0
    )
ON CONFLICT (carrito_id) DO NOTHING;

-- ITEMS: este bloque será reemplazado por la lista concreta de INSERTs
{{ITEMS}}

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
    c.carrito_id = '{{CART_ID}}';

COMMIT;