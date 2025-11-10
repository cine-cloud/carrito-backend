# 🌐 Servicios Web

> **Documentación técnica sobre servicios web RESTful con Spring Boot**

## 📚 Tabla de Contenidos

- [¿Qué es un Servicio Web?](#qué-es-un-servicio-web)
- [Reglas REST](#reglas-generales-para-nombres-de-uris-api-rest)
- [Códigos HTTP](#códigos-de-respuesta-recomendados)
- [Spring Boot](#spring-framework-spring-boot)
- [Testing](#testing-integración-servicios-web)
- [Maven](#maven-compiler-and-builder)

---

## ¿Qué es un Servicio Web?

En términos de arquitectura de software, un **servicio** es una aplicación o proceso que se encuentra escuchando en un determinado host y puerto, esperando recibir solicitudes de otros programas (clientes). 

> **Un servicio web** es un tipo especial de servicio que:
> 
> - Utiliza protocolos web como **HTTP** o **HTTPS** para comunicarse
> - Expone su funcionalidad a través de **URLs**
> - Se llama web porque se construye sobre tecnologías propias de la web (como HTTP, URIs y formatos como JSON o XML)

### 💡 Los servicios web permiten:

- Separar el **frontend** (cliente) del **backend** (servidor)
- Reutilizar lógica de negocio o datos en distintas interfaces (web, móvil, otros sistemas)

---

## Reglas Generales para Nombres de URIs API REST

### ✅ Buenas Prácticas

> [!IMPORTANT]
> - Usar nombres de recursos en **plural**
> - Usar nombres **sustantivos**, no verbos
> - Evitar extensiones como `.json`, `.xml` en la URI
> - **El verbo va en el método HTTP, no en la URI**

---

### 🔸 GET

| Acción            | URI ejemplo          | Descripción          |
|-------------------|----------------------|----------------------|
| Obtener todos     | `GET /users`         | Lista de usuarios    |
| Obtener uno       | `GET /users/{id}`    | Usuario por ID       |
| Sub-recursos      | `GET /users/{id}/posts` | Posts del usuario |
| Filtro con query params | `GET /products?category=zapatos` | Filtrar productos por categoría |

---

### 🔸 POST

| Acción            | URI ejemplo                  |Descripción                         |
|-------------------|------------------------------|------------------------------------|
| Crear recurso     | `POST /users`                | Crear un nuevo usuario             |
| Crear sub-recurso | `POST /users/{id}/telefonos` | Crear un teléfono para ese usuario |


---

### 🔸 PUT

| Acción             | URI ejemplo       | Descripción                        |
|--------------------|-------------------|------------------------------------|
| Reemplazar recurso | `PUT /users/{id}` | Reemplaza completamente al usuario |

---

### 🔸 DELETE

| Acción           | URI ejemplo          | Descripción             |
|------------------|----------------------|-------------------------|
| Eliminar recurso | `DELETE /users/{id}` | Borra un usuario por ID |

---

### 🔸 Otros Casos

| Caso           | URI ejemplo                | Descripción        |
|----------------|----------------------------|--------------------|
| Login          | `POST /auth/login`         | Autenticación      |
| Logout         | `POST /auth/logout`        | Cierre de sesión   |
| Acción puntual | `POST /orders/{id}/cancel` | Cancelar una orden |

---

## ✅ Códigos de Respuesta Recomendados

| Método   | Código recomendado          | Cuándo usarlo                        |
|----------|-----------------------------|--------------------------------------|
| `GET`    | `200 OK`                    | Recurso(s) obtenido(s) correctamente |
| `POST`   | `201 Created`               | Recurso creado exitosamente          |
| `PUT`    | `200 OK` / `204 No Content` | Actualización o creación de recurso  |
| `DELETE` | `200 OK` / `204 No Content` | Eliminación exitosa sin contenido    |

---

## Spring Framework (Spring Boot)

> [!NOTE]
> Cuando nos acoplamos a un framework, tenemos muchas ventajas, pero también estamos atados a su schedule.
> Ver: [Spring Boot Support](https://spring.io/projects/spring-boot#support)

### 🚀 Características de Spring

- **Spring** nació como un framework de **Inyección de Dependencias**
- Se encarga de instanciar e inyectar colaboradores en los objetos
- No cualquier objeto: básicamente los servicios, conexión a la BD, etc.
- Las entidades, value objects no lo puede hacer, son stateful
- Módulos como **SpringMVC**, **SpringData**, **SpringSecurity**, etc. se apoyan en el core de Spring
- **SpringBoot**: simplifica la configuración de todos los módulos para que funcione "out of the box"

---

### 📌 Main Class

Anotada con `@SpringBootApplication` que es una combinación de:

| Anotación                  | Descripción                                                                                                  |
|----------------------------|--------------------------------------------------------------------------------------------------------------|
| `@Configuration`           | Indica que la clase puede contener definiciones de beans                                                     |
| `@EnableAutoConfiguration` | Habilita la configuración automática de Spring Boot basada en las dependencias presentes en el classpath     |
| `@ComponentScan`           | Define sobre qué paquetes se escanea para encontrar clases anotadas para inyectar                            |

---

### 🎮 Rest Controllers

> Capa muy fina que expone la capa de servicios como servicios web/http.

**Anotaciones principales:**

| Anotación                     | Uso                                               |
|-------------------------------|---------------------------------------------------|
| `@RestController`             | Marca la clase como controlador REST              |
| `@GetMapping` / `@PostMapping` | Define el método HTTP del endpoint              |
| `@RequestParam`               | Extrae parámetros de query string (`?key=value`) |
| `@PathVariable`               | Extrae valores de la URI (`/users/{id}`)         |
| `@RequestBody`                | Deserializa el cuerpo JSON a un objeto            |

> [!TIP]
> El retorno del método se convierte automáticamente a JSON y se envía en la respuesta HTTP.

---

### 📤 Returning JSON

> [!NOTE]
> SpringBoot se encarga de hacerlo por defecto usando **Jackson**.

**Requisitos:**
- Las clases deben tener **getters** para que Jackson pueda serializar los atributos
- Aquí es donde hay que definir si queremos acoplar nuestro modelo de dominio a los clientes REST o no

---

### ⚠️ Exception Handling Global

Queremos manejar las excepciones de forma global:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Manejo de la excepción
    }
}
```

**Anotaciones clave:**
- `@RestControllerAdvice` → Clase que maneje excepciones globalmente
- `@ExceptionHandler(Exception.class)` → Método para manejar excepciones específicas

---

## 🧪 Testing Integración Servicios Web

### Profiles

> [!NOTE]
> Necesito crear instancias de objetos diferentes para tests de integración que para producción.

### MockMvc and WebTestClient

📖 Referencia: [Spring Testing Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

| Herramienta       | Descripción                                                                                          |
|-------------------|------------------------------------------------------------------------------------------------------|
| **MockMvc**       | Ejecuta el controller y todo el stack en memoria, sin servidor, sin red. Perfecto para tests rápidos |
| **WebTestClient** | Levanta un server real con `@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT/RANDOM_PORT)` |

### ✅ ¿Qué podemos testear de la capa web?

Teniendo Tests escritos unitarios y de integración a nivel servicio:

- ✔️ Que pasa si llegan o no llegan los parámetros (query params, path variables, body)
- ✔️ Que retorne el JSON que esperamos en el formato que esperamos
- ✔️ Que retorne errores en el formato que esperamos (manejo de exceptions)

---

## 🔧 Maven: Compiler and Builder

### Configuración

- Configuro el compilador con **Lombok** y el flag `-parameters`
- Agrego el plugin de `spring-boot-maven-plugin` que me permite entre otras cosas ejecutar la app con `mvn spring-boot:run`

### 💻 Comandos Útiles

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run

# Empaquetar en JAR
mvn clean package

# Ejecutar tests
mvn test

# Instalar en repositorio local
mvn install
```

---

> **Documentación creada para el proyecto Carrito de Película - Backend**