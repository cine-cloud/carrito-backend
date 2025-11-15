# Levantar Películas y Carrito usando un único RabbitMQ (RabbitMQ solo del Carrito)

## Puertos importantes

| Servicio | Puerto |
|----------|--------|
| Películas | 8080 |
| Carrito | 8081 |
| RabbitMQ (Carrito) broker | 5672 |
| RabbitMQ UI | 15672 |
| PostgreSQL Carrito (host) | 5433 |
| PostgreSQL Películas (cont.) | 5432 |

---

## 1) Levantar RabbitMQ y la base del Carrito

```powershell
cd C:\Users\eva_g\.vscode\workspaces\carrito-de-pelicula-backend
docker-compose up -d rabbitmq carritos-db
```
o simplemente el comando:
docker-compose up -d 
levanta ambos contenedores.

Verificar:

```powershell
docker ps --filter "name=rabbitmq-container"
docker ps --filter "name=carritos-db-container"
```

RabbitMQ UI → http://localhost:15672 (guest / guest)

---

## 2) Levantar Carrito

```powershell
cd C:\Users\eva_g\.vscode\workspaces\carrito-de-pelicula-backend
docker-compose up -d carritos-app
docker-compose logs -f carritos-app
```

Carrito declara automáticamente:

- Exchange: `pelicula_exchange`
- Queue: `carrito_pelicula_queue`
- Routing key: `pelicula.evento`

---

## 3) Levantar PELÍCULAS con Docker (SIN RabbitMQ propio)

Se usa el RabbitMQ del Carrito mediante `host.docker.internal`.

```powershell
cd C:\Users\eva_g\.vscode\workspaces\peliculas-backend
docker-compose up -d peliculas-db peliculas-app gateway
```

Logs:

```powershell
docker-compose logs -f peliculas-app
```

Swagger → http://localhost:8080/swagger-ui/index.html

---

## 4) Prueba completa del flujo RabbitMQ

### 1) Películas → Editar película

```
PUT /api/peliculas/{id}
```

Esto publica un evento.

### 2) RabbitMQ → Ver cola

UI → Queue `carrito_pelicula_queue`

### 3) Carrito → Ver actualización

```
POST /carrito/{usuarioId}
POST /carrito/{idCarrito}/items
GET /carrito/{idCarrito}
```

---

## 5) Problemas comunes

### Contenedores viejos:

```powershell
docker stop peliculas-app-container rabbitmq-container 2>$null
docker rm -f peliculas-app-container rabbitmq-container
```

### Ver conexión RabbitMQ:

`SPRING_RABBITMQ_HOST = host.docker.internal`

### Ver que Carrito consume:

```powershell
docker-compose logs -f carritos-app
```

