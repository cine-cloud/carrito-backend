version: '3.9'
services:
  mysql:
    image: mysql:8.4
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: peliculas
      MYSQL_USER: app
      MYSQL_PASSWORD: app
    ports: ["3306:3306"]
    command: ["--default-authentication-plugin=mysql_native_password"]
    volumes:
      - mysqldata:/var/lib/mysql

  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: app
      RABBITMQ_DEFAULT_PASS: app
    ports: ["5672:5672","15672:15672"]

  peliculas:
    build: ./catalogo-de-peliculas-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/peliculas?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: app
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_USERNAME: app
      SPRING_RABBITMQ_PASSWORD: app
    depends_on: [mysql, rabbitmq]
    ports: ["8081:8080"]

  carritos:
    build: ./carrito-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/peliculas?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: app
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_USERNAME: app
      SPRING_RABBITMQ_PASSWORD: app
      PELICULAS_BASE_URL: http://peliculas:8080
    depends_on: [mysql, rabbitmq, peliculas]
    ports: ["8082:8080"]

volumes:
  mysqldata:
