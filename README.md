# Carrito Backend

## Descripción de Propósito
Este vertical gestiona el carrito de compras de los clientes. Permite agregar películas, quitarlas, visualizar el subtotal/total aplicando descuentos, y finalmente realizar la compra (checkout).

## Servicios que Expone vía HTTP
- `GET /carrito`: Obtiene los ítems actuales del carrito del usuario autenticado.
- `POST /carrito/item`: Agrega una película al carrito.
- `DELETE /carrito/item/{id}`: Quita una película del carrito.
- `POST /carrito/checkout`: Confirma la compra del carrito actual.

## Eventos que Publica o Consume (RabbitMQ)
- **Publica**: Evento `CompraRealizada` cuando un cliente ejecuta el checkout de su carrito exitosamente.

## Diagramas C4

```mermaid
C4Context
    title Diagrama C4 - Carrito Backend
    
    Container(api_gateway, "API Gateway", "Gateway", "Enruta peticiones")
    System_Ext(rabbitmq, "RabbitMQ", "Message Broker")
    
    System_Boundary(b1, "Vertical Carrito") {
      Container(carrito_api, "Carrito API", "API REST", "Gestiona el carrito de compras")
      ContainerDb(carrito_db, "Base de Datos", "Relacional/NoSQL", "Almacena carritos activos")
    }

    Rel(api_gateway, carrito_api, "Peticiones de carrito", "REST/HTTP")
    Rel(carrito_api, carrito_db, "Lee/Escribe", "TCP")
    Rel(carrito_api, rabbitmq, "Publica evento CompraRealizada", "AMQP")
```
