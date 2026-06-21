# Sistema de Reservas de Canchas Deportivas

Arquitectura de microservicios para la gestión de reservas de canchas deportivas, desarrollada con Spring Boot, Spring Cloud y seguridad JWT.

## Integrantes del equipo

- Rodrigo Vidal

## Contexto del proyecto

Sistema backend para la reserva de canchas deportivas que permite a propietarios registrar sus canchas y horarios disponibles, y a clientes realizar reservas con pago integrado. El acceso está controlado mediante autenticación JWT con roles diferenciados (ADMIN, PROPIETARIO, CLIENTE).

## Microservicios implementados

| Servicio | Puerto | Descripción |
|---|---|---|
| eureka-server | 8761 | Service Discovery (Eureka) |
| api-gateway | 8080 | Punto de entrada único con validación JWT |
| auth-service | 8081 | Autenticación y generación de tokens JWT |
| usuario-service | 8082 | Gestión de usuarios y perfiles |
| cancha-service | 8083 | Gestión de canchas deportivas |
| horario-service | 8084 | Gestión de horarios disponibles |
| reserva-service | 8085 | Orquestador de reservas (consume 4 servicios vía Feign) |
| pago-service | 8086 | Procesamiento de pagos de reservas |

## Rutas principales del API Gateway (puerto 8080)

| Prefijo | Servicio destino |
|---|---|
| `/auth/**` | auth-service (público, sin JWT) |
| `/usuarios/**` | usuario-service |
| `/canchas/**` | cancha-service |
| `/horarios/**` | horario-service |
| `/reservas/**` | reserva-service |
| `/pagos/**` | pago-service |

## Documentación Swagger/OpenAPI

Con los servicios corriendo localmente, acceder a:

| Servicio | URL Swagger UI |
|---|---|
| auth-service | http://localhost:8081/swagger-ui/index.html |
| usuario-service | http://localhost:8082/swagger-ui/index.html |
| cancha-service | http://localhost:8083/swagger-ui/index.html |
| horario-service | http://localhost:8084/swagger-ui/index.html |
| reserva-service | http://localhost:8085/swagger-ui/index.html |
| pago-service | http://localhost:8086/swagger-ui/index.html |

## Instrucciones de ejecución local (con XAMPP/MySQL)

### Prerrequisitos

- Java 17+
- Maven 3.8+
- MySQL corriendo en puerto 3306 (usuario: root, contraseña: root)
- XAMPP o MySQL Server activo

### Orden de arranque

Levantar los servicios en este orden:

1. **eureka-server**
2. **auth-service**, **usuario-service**, **cancha-service**, **horario-service**, **pago-service**
3. **reserva-service**
4. **api-gateway**

### Comandos Maven (desde la carpeta de cada servicio)

```bash
# Ejemplo para usuario-service
cd usuario-service
mvn spring-boot:run

# Ejemplo para servicios anidados (cancha-service, horario-service, etc.)
cd cancha-service/cancha-service
mvn spring-boot:run
```

### Ejecutar pruebas unitarias

```bash
# Desde la carpeta del servicio a testear
cd usuario-service
mvn test

cd cancha-service/cancha-service
mvn test
```

## Instrucciones de ejecución con Docker

### Prerrequisitos

- Docker Desktop instalado y corriendo
- Maven instalado (para compilar antes de construir imágenes)

### Pasos

1. Compilar cada servicio (generar JAR):
```bash
cd eureka-server && mvn package -DskipTests
cd ../api-gateway/api-gateway && mvn package -DskipTests
cd ../../auth-service/auth-service && mvn package -DskipTests
cd ../../usuario-service && mvn package -DskipTests
cd ../cancha-service/cancha-service && mvn package -DskipTests
cd ../../horario-service/horario-service && mvn package -DskipTests
cd ../../reserva-service/reserva-service && mvn package -DskipTests
cd ../../pago-service/pago-service && mvn package -DskipTests
cd ../../
```

2. Levantar toda la arquitectura con Docker Compose:
```bash
docker-compose up --build
```

3. Para detener:
```bash
docker-compose down
```

## Seguridad JWT

El API Gateway valida el token JWT en cada petición (excepto `/auth/**`). Los roles controlan el acceso:

- **ADMIN**: Acceso total al sistema
- **PROPIETARIO**: Puede gestionar canchas y horarios
- **CLIENTE**: Puede realizar y consultar sus reservas

### Flujo de autenticación

```
POST /auth/register  →  Registro con rol
POST /auth/login     →  Obtener token JWT
Authorization: Bearer <token>  →  Incluir en headers de las demás peticiones
```

## Comunicación entre microservicios

El `reserva-service` actúa como orquestador usando **OpenFeign**:

```
POST /reservas
  ├─ UsuarioClient  → GET /usuarios/{id}      (valida que el cliente exista)
  ├─ CanchaClient   → GET /canchas/{id}       (obtiene precio de la cancha)
  ├─ HorarioClient  → GET /horarios/{id}      (valida disponibilidad)
  ├─ PagoClient     → POST /pagos             (crea el pago)
  └─ HorarioClient  → PUT /horarios/{id}/disponibilidad?disponible=false
```

## Tecnologías utilizadas

- **Java 17** + **Spring Boot 4.1.0**
- **Spring Cloud 2025.1.2** (Eureka + Gateway + OpenFeign)
- **Spring Security** + **JJWT** (autenticación JWT)
- **Spring Data JPA** + **MySQL 8**
- **springdoc-openapi 2.8.8** (Swagger UI)
- **JUnit 5** + **Mockito** (pruebas unitarias)
- **Docker** + **Docker Compose** (despliegue en contenedores)
- **Lombok** (reducción de código boilerplate)
