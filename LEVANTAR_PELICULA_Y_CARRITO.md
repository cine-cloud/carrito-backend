## Levantar Películas y Carrito usando un único RabbitMQ (desde Carrito)

Importante: RabbitMQ se levanta solo desde el proyecto Carrito. No lo levantes en Películas.

### Puertos
- Películas: 8080
- Carrito: 8081
- RabbitMQ broker: 5672
- UI RabbitMQ: 15672 (usuario: guest / pass: guest)
- PostgreSQL Carrito (host): 5433 → contenedor: 5432

### 1) Levantar RabbitMQ y la BD de Carrito (una sola vez)
Ejecuta en PowerShell, línea por línea:

```powershell
cd C:\Users\eva_g\.vscode\workspaces\carrito-de-pelicula-backend
docker-compose up -d rabbitmq carritos-db
```

Verificación rápida:
```powershell
docker ps --filter "name=rabbitmq-container"
docker ps --filter "name=carritos-db-container"
```
UI RabbitMQ: `http://localhost:15672` (guest/guest)

### 2) Levantar la app de Carrito
```powershell
cd C:\Users\eva_g\.vscode\workspaces\carrito-de-pelicula-backend
docker-compose up -d carritos-app
docker-compose logs -f carritos-app
```
Swagger Carrito: `http://localhost:8081/swagger-ui/index.html`

Al iniciar, Carrito declara automáticamente:
- Exchange: `pelicula_exchange`
- Queue: `carrito_pelicula_queue`
- Binding: routing key `pelicula.evento`

Puedes verlo en la UI: Exchanges → `pelicula_exchange` → pestaña “Bindings”.

### 3) Levantar Películas (usando el mismo Rabbit)
Recomendado: correr local con Maven (no Docker para evitar un segundo RabbitMQ):
```powershell
cd C:\Users\eva_g\.vscode\workspaces\peliculas-backend
mvn spring-boot:run
```
Swagger Películas: `http://localhost:8080/swagger-ui/index.html`

Nota: Si alguna vez usas docker-compose de Películas, no inicies su servicio `rabbitmq`.

### 4) Prueba end‑to‑end
1. En Películas: crear o editar una película (POST/PUT `/api/peliculas`) cambiando el campo `precio`.
2. En RabbitMQ: cola `carrito_pelicula_queue` → “Get messages” (si Carrito está corriendo, puede quedar en 0 porque consume al instante).
3. En Carrito: 
   - Crear carrito: `POST /carrito/{usuarioId}`
   - Agregar ítem: `POST /carrito/{idCarrito}/items` con body `{ "peliculaId": <id>, "cantidad": 2 }`
   - Ver carrito: `GET /carrito/{idCarrito}` → el `precioUnitario` del ítem debería reflejar el precio actualizado.

### 5) Problemas comunes
- Conflicto de nombre `rabbitmq-container`:
  ```powershell
  docker stop rabbitmq-container 2>$null
  docker rm -f rabbitmq-container
  docker-compose up -d rabbitmq
  ```
- No aparecen colas/exchanges: asegúrate de que `carritos-app` esté iniciado y conectado antes de probar.
- PostgreSQL desde la extensión: conecta a `127.0.0.1:5433`, BD `carritos_db`, user `user_carritos`, pass `pass_carritos`.
