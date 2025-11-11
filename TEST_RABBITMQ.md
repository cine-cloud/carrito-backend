# 🐰 Prueba de RabbitMQ - Carrito y Películas

## 📋 Requisitos Previos

1. **RabbitMQ debe estar corriendo** (en el proyecto de películas)
2. **Ambos servicios deben estar levantados**

## 🚀 Pasos para Probar

### 1. Levantar el servicio de Películas (con RabbitMQ)

```bash
cd peliculas-backend
docker-compose up -d
```

Esto levanta:
- ✅ PostgreSQL (puerto 5432)
- ✅ RabbitMQ (puerto 5672 y 15672 para la UI)
- ✅ Servicio de Películas (puerto 8080)

**Verificar RabbitMQ:**
- Abrir navegador: http://localhost:15672
- Usuario: `guest` / Password: `guest`
- Deberías ver el exchange `pelicula_exchange` creado

### 2. Levantar el servicio de Carrito

```bash
cd carrito-de-pelicula-backend
docker-compose up -d
```

Esto levanta:
- ✅ PostgreSQL (puerto 5432)
- ✅ Servicio de Carrito (puerto 8081)
- ✅ Gateway (puerto 9500)

### 3. Verificar que ambos servicios estén conectados a RabbitMQ

**En los logs del servicio de Películas deberías ver:**
```
✅ Exchange 'pelicula_exchange' creado
```

**En los logs del servicio de Carrito deberías ver:**
```
✅ Queue 'carrito_queue' creada
✅ Binding configurado: carrito_queue -> pelicula_exchange (routing key: pelicula.evento)
✅ Listener activo en carrito_queue
```

**Ver logs:**
```bash
docker logs carritos-app-container --tail 50
```

## 🎬 Flujo Completo de Prueba con Swagger UI

> **💡 Swagger UI te permite probar todos los endpoints directamente desde el navegador:**
> - **Carrito Swagger UI:** http://localhost:8081/swagger-ui.html
> - **Películas Swagger UI:** http://localhost:8080/swagger-ui.html
> - **OpenAPI JSON:** http://localhost:8081/v3/api-docs

### Parte 1: Gestión de Películas

#### 1.1 Crear una película

1. Abre http://localhost:8080/swagger-ui.html
2. Busca `POST /api/peliculas`
3. Haz clic en "Try it out"
4. En el body, pega este JSON:
   ```json
   {
     "titulo": "Matrix",
     "precio": 1500.00,
     "fechaSalida": "1999-03-31",
     "condicion": "NUEVO",
     "formato": "DVD",
     "sinopsis": "Un hacker descubre la verdad sobre la realidad"
   }
   ```
5. Haz clic en "Execute"
6. **Guarda el `peliculaId` de la respuesta** (lo necesitarás después)

#### 1.2 Crear otra película

1. En el mismo endpoint `POST /api/peliculas`
2. Cambia el body por:
   ```json
   {
     "titulo": "Inception",
     "precio": 2000.00,
     "fechaSalida": "2010-07-16",
     "condicion": "NUEVO",
     "formato": "BLU_RAY",
     "sinopsis": "Un ladrón que roba secretos del subconsciente"
   }
   ```
3. Ejecuta
4. **Guarda el `peliculaId` de esta segunda película**

#### 1.3 Modificar una película (esto disparará el evento RabbitMQ)

1. Busca `PUT /api/peliculas/{id}`
2. Haz clic en "Try it out"
3. Ingresa el ID de la película (ej: el ID de Matrix)
4. En el body, modifica el precio:
   ```json
   {
     "titulo": "Matrix",
     "precio": 2500.00,
     "fechaSalida": "1999-03-31",
     "condicion": "NUEVO",
     "formato": "DVD",
     "sinopsis": "Un hacker descubre la verdad sobre la realidad"
   }
   ```
5. Ejecuta

**Verifica en los logs del servicio de Carrito:**
```bash
docker logs carritos-app-container --tail 20
```

Deberías ver:
```
📩 Evento recibido del servicio Película:
   - ID: {peliculaId}
   - Título: Matrix
   - Precio: 2500.00
```

#### 1.4 Ver todas las películas

1. Busca `GET /api/peliculas` o `GET /api/catalogo`
2. Haz clic en "Try it out" → "Execute"

### Parte 2: Gestión del Carrito

#### 2.1 Crear un carrito

1. Abre http://localhost:8081/swagger-ui.html
2. Busca `POST /carrito/{usuarioId}`
3. Haz clic en "Try it out"
4. Ingresa un `usuarioId` (ej: `usuario123`)
5. Ejecuta
6. **Guarda el `id` del carrito** de la respuesta (es un UUID)

#### 2.2 Agregar película al carrito

1. Busca `POST /carrito/{idCarrito}/items`
2. Haz clic en "Try it out"
3. Ingresa el `idCarrito` (el UUID que guardaste)
4. En el body:
   ```json
   {
     "peliculaId": 1,
     "cantidad": 2
   }
   ```
   > **Nota:** Reemplaza `1` con el `peliculaId` real que obtuviste al crear la película
5. Ejecuta

#### 2.3 Agregar otra película al carrito

1. En el mismo endpoint `POST /carrito/{idCarrito}/items`
2. Usa el mismo `idCarrito`
3. En el body, usa el ID de la segunda película:
   ```json
   {
     "peliculaId": 2,
     "cantidad": 1
   }
   ```
4. Ejecuta

#### 2.4 Ver el carrito actual

1. Busca `GET /carrito/{idCarrito}`
2. Haz clic en "Try it out"
3. Ingresa el `idCarrito`
4. Ejecuta

**O desde el navegador directamente:**
```
http://localhost:8081/carrito/{idCarrito}
```

#### 2.5 Modificar cantidad de un item en el carrito

1. Busca `PUT /carrito/{idCarrito}/items/{peliculaId}`
2. Haz clic en "Try it out"
3. Ingresa `idCarrito` y `peliculaId`
4. En el body:
   ```json
   {
     "cantidad": 3
   }
   ```
5. Ejecuta

#### 2.6 Eliminar un item del carrito

1. Busca `DELETE /carrito/{idCarrito}/items/{peliculaId}`
2. Haz clic en "Try it out"
3. Ingresa `idCarrito` y `peliculaId`
4. Ejecuta

### Parte 3: Verificar Sincronización RabbitMQ ⚡

#### 3.1 Modificar una película que está en el carrito

**Este es el paso clave para probar RabbitMQ:**

1. En Swagger de Películas (http://localhost:8080/swagger-ui.html)
2. Busca `PUT /api/peliculas/{id}`
3. Ingresa el ID de una película que agregaste al carrito (ej: Matrix)
4. En el body, cambia el precio:
   ```json
   {
     "titulo": "Matrix",
     "precio": 3000.00,
     "fechaSalida": "1999-03-31",
     "condicion": "NUEVO",
     "formato": "DVD",
     "sinopsis": "Un hacker descubre la verdad sobre la realidad"
   }
   ```
5. Ejecuta

#### 3.2 Verificar los logs del servicio de Carrito

```bash
docker logs carritos-app-container --tail 30
```

**Deberías ver:**
```
📩 Evento recibido del servicio Película:
   - ID: {peliculaId}
   - Título: Matrix
   - Precio: 3000.00
   ✅ Precio actualizado en 1 carrito(s) abierto(s)
```

#### 3.3 Verificar que el precio se actualizó en el carrito

1. En Swagger de Carrito (http://localhost:8081/swagger-ui.html)
2. Busca `GET /carrito/{idCarrito}`
3. Ingresa el `idCarrito`
4. Ejecuta

**El precio unitario de Matrix debería ser ahora 3000.00 y el total debería haberse recalculado automáticamente.**

### Parte 4: Operaciones Adicionales

#### 4.1 Vaciar el carrito

1. Busca `DELETE /carrito/{idCarrito}/items`
2. Ingresa el `idCarrito`
3. Ejecuta

#### 4.2 Confirmar carrito (checkout)

1. Primero agrega items al carrito (si está vacío)
2. Busca `POST /carrito/{idCarrito}/confirmar`
3. Ingresa el `idCarrito`
4. Ejecuta

#### 4.3 Eliminar carrito

1. Busca `DELETE /carrito/{idCarrito}`
2. Ingresa el `idCarrito`
3. Ejecuta

## ✅ Checklist de Verificación

- [ ] Crear películas
- [ ] Modificar películas
- [ ] Crear carrito
- [ ] Agregar películas al carrito
- [ ] Modificar cantidad de items en el carrito
- [ ] Eliminar items del carrito
- [ ] **Modificar una película que está en el carrito y verificar que el precio se actualiza automáticamente (RabbitMQ)**
- [ ] Vaciar carrito
- [ ] Confirmar carrito
- [ ] Eliminar carrito

## 🔍 Verificar en RabbitMQ Management UI

1. Abrir http://localhost:15672
2. Ir a **Exchanges** → Verificar que existe `pelicula_exchange`
3. Ir a **Queues** → Verificar que existe `carrito_queue`
4. Ir a **Bindings** → Verificar que `carrito_queue` está vinculada a `pelicula_exchange` con routing key `pelicula.evento`

## 🐛 Troubleshooting

### El listener no recibe mensajes
- Verificar que RabbitMQ está corriendo: `docker ps | grep rabbitmq`
- Verificar logs del servicio de carrito: `docker logs carritos-app-container`
- Verificar que el exchange y queue están creados en RabbitMQ UI

### Error de conexión a RabbitMQ
- Verificar que el puerto 5672 está disponible
- Verificar que `SPRING_RABBITMQ_HOST` está configurado correctamente
- En Docker: usar `host.docker.internal` para conectarse al RabbitMQ del host

### Los mensajes no llegan
- Verificar que el routing key coincide: debe ser `pelicula.evento`
- Verificar que el exchange es `pelicula_exchange` (no `peliculas.exchange`)
- Verificar que la queue `carrito_queue` está vinculada correctamente

## ✅ Checklist de Verificación Final

- [ ] RabbitMQ corriendo (puerto 5672)
- [ ] Exchange `pelicula_exchange` creado
- [ ] Queue `carrito_queue` creada
- [ ] Binding configurado correctamente
- [ ] Listener activo en carrito
- [ ] Al crear/editar película, se publica evento
- [ ] Al recibir evento, se actualizan precios en carritos abiertos
