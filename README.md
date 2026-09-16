# ExpresoFast — Laboratorios 6 y 7 (Seguridad JWT/RBAC + Suite de Pruebas)

**Universidad de Costa Rica — Sede del Atlántico, Recinto Paraíso**
**Carrera de Informática Empresarial**
**Curso:** IF0009 — Desarrollo de Software IV
**Profesor:** Mag. Jonathan Granados C.
**Semestre:** II-2026

**Estudiante:** Juan Pablo Solano Vásquez
**Carné:** C5K023

> **Laboratorio 7** (suite de pruebas unitarias e integración con JUnit 5, Mockito, MockMvc y
> JaCoCo) se desarrolla en la rama `lab07`. Ver la sección *Pruebas Automatizadas* al final.

---

## Descripción

Plataforma full-stack de logística y monitoreo de envíos express. Esta segunda parte extiende
el proyecto del Laboratorio 5 agregando autenticación con JWT, control de acceso por roles
(RBAC), DTOs con validación estricta, manejo centralizado de excepciones y una bitácora de
auditoría de cambios de estado.

## Requisitos de Entorno

| Herramienta | Versión utilizada |
|---|---|
| Java | 21+ (probado con JDK 25) |
| Maven | 3.9.x (o el wrapper `mvnw` incluido) |
| Spring Boot | 4.1.1 |
| Microsoft SQL Server | Developer Edition (probado con versión 2022 / motor 17.0) |
| Navegador | Cualquier navegador moderno con soporte de Fetch API (Chrome, Brave, Firefox, Edge) |

## Estructura del Repositorio

```
expresofast-lab6-c5k023/
├── backend/                          # API REST (Spring Boot)
│   ├── src/
│   ├── pom.xml
│   └── application.properties.template
├── database/                         # Scripts SQL, en orden de ejecución
│   ├── 01_schema_lab5.sql
│   ├── 02_schema_lab6_extension.sql
│   ├── 03_data_seeds.sql
│   └── 04_schema_lab7_extension.sql
├── frontend/                         # HTML5 + CSS3 + JS vanilla
│   ├── login.html
│   ├── index.html
│   ├── styles.css
│   ├── auth.js
│   └── app.js
├── docs/
│   └── ExpresoFast_Postman_Collection.json
└── README.md
```

## Guía de Configuración de Base de Datos

1. Abrí SSMS y conectate a tu instancia local de SQL Server.
2. Ejecutá los 3 scripts de la carpeta `database/`, **en orden**:
   - `01_schema_lab5.sql` — crea la base de datos `ExpresoFastC5K023_II2026` y las tablas del dominio logístico (`EmpresaLogistica`, `Vehiculo`, `Conductor`, `Envio`).
   - `02_schema_lab6_extension.sql` — agrega las tablas de seguridad y auditoría (`Usuario`, `Rol`, `UsuarioRol`, `BitacoraEnvio`).
   - `03_data_seeds.sql` — inserta datos de prueba: 1 empresa, 3 vehículos, 2 conductores, los 3 roles del sistema y 3 usuarios de prueba con contraseñas ya encriptadas en BCrypt.
   - `04_schema_lab7_extension.sql` — **(Laboratorio 7)** agrega la columna `activo` a `Conductor` y `conductor_asignado_id` a `Vehiculo`, necesarias para las reglas de negocio de asignación de conductores.

## Configuración del Backend

1. Copiá `backend/application.properties.template` a `backend/src/main/resources/application.properties`.
2. Completá los valores marcados como `CAMBIAR_POR_...`:
   - `spring.datasource.password`: la contraseña de tu usuario `sa` de SQL Server.
   - `app.jwt.secret`: cualquier cadena de al menos 32 caracteres (usada para firmar los tokens JWT).
3. Este archivo con tus credenciales reales **no se sube al repositorio** (está excluido en `.gitignore`).

## Usuarios de Prueba

Todos con la misma contraseña: **`Password123!`**

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Password123!` | `ROLE_ADMIN` |
| `operador1` | `Password123!` | `ROLE_OPERADOR` |
| `conductor1` | `Password123!` | `ROLE_CONDUCTOR` |

## Matriz de Permisos (RBAC)

| Endpoint | Método | Roles permitidos |
|---|---|---|
| `/api/auth/login` | POST | Público |
| `/api/envios/optimizados` | GET | ADMIN, OPERADOR, CONDUCTOR |
| `/api/envios` | POST | ADMIN, OPERADOR |
| `/api/envios/{id}/estado` | PATCH | ADMIN, CONDUCTOR |
| `/api/envios/{id}/bitacora` | GET | ADMIN, OPERADOR |
| `/api/vehiculos/**` | Todos | ADMIN |

## Instrucciones de Ejecución

### Backend

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Frontend

El frontend es HTML/CSS/JS estático, sin build. Abrí `frontend/login.html` con una extensión
tipo **Live Server** (VS Code) o cualquier servidor estático local — no lo abras con doble clic
directo desde el explorador de archivos, porque las peticiones `fetch` requieren que se sirva
por HTTP, no por `file://`.

1. Iniciá el backend primero.
2. Abrí `frontend/login.html`.
3. Iniciá sesión con cualquiera de los usuarios de prueba de la tabla anterior.

### Colección de Postman

Importá `docs/ExpresoFast_Postman_Collection.json` en Postman o Thunder Client para probar
todos los endpoints directamente. Incluye una variable `{{jwt_token}}` que hay que completar
manualmente con el token devuelto por `/api/auth/login`.

## Funcionalidades Extra (Reto Autónomo)

- **Bloqueo de transición de estado inválida:** un envío en estado `ENTREGADO` o `CANCELADO`
  no puede volver a `PENDIENTE` o `EN_TRANSITO`. El intento devuelve `400 Bad Request`.
- **Filtro de fechas en la bitácora:** el modal de historial de un envío permite filtrar las
  entradas por un rango de fechas (desde/hasta).


---

# Laboratorio 7 — Suite de Pruebas de Cero Tolerancia a Defectos

Esta etapa certifica la plataforma con una suite automatizada de pruebas unitarias y de
integración en capa web, con umbral obligatorio de cobertura.

## Herramientas de Prueba

| Herramienta | Versión | Uso |
|---|---|---|
| JUnit 5 (Jupiter) | incluido en `spring-boot-starter-test` | Motor de pruebas |
| Mockito | incluido en `spring-boot-starter-test` | Aislamiento de dependencias (`@Mock`, `@InjectMocks`) |
| MockMvc | incluido en `spring-boot-starter-test` | Pruebas de corte de controlador (`@WebMvcTest`) |
| maven-surefire-plugin | 3.2.5 | Ejecución de las pruebas |
| jacoco-maven-plugin | 0.8.11 | Reporte y verificación de cobertura (mínimo 85%) |

## Clases de Prueba Implementadas

### Pruebas unitarias de servicios (`src/test/java/.../lab06/business/`)

| Clase | Casos cubiertos |
|---|---|
| `EnvioServiceTest` | Creación exitosa con estado inicial `PENDIENTE`; `CapacidadExcedidaException` por sobrepeso; transición inválida `ENTREGADO → EN_TRANSITO`; cancelación bloqueada de envíos `EN_TRANSITO`; registro automático en bitácora; cálculo de tarifas parametrizado |
| `VehiculoServiceTest` | `DuplicateResourceException` por placa duplicada; bloqueo de asignación de conductor inactivo; alta, consulta, actualización y eliminación |
| `EmpresaLogisticaServiceTest` | `DuplicateResourceException` por cédula jurídica duplicada; asignación automática de fecha de registro; recurso no encontrado |
| `AuthServiceTest` | Login exitoso con emisión de token JWT; propagación de `BadCredentialsException`; usuario inexistente |

### Pruebas de capa web (`src/test/java/.../lab06/controller/`)

| Clase | Casos cubiertos |
|---|---|
| `EnvioControllerTest` | `GET /api/envios/{id}` → 200 con validación de `jsonPath`; envío inexistente → 404 en formato RFC 7807; `POST /api/envios` válido → 201; payload inválido → 400 con lista de errores de validación |
| `AuthControllerTest` | `POST /api/auth/login` con credenciales correctas → 200 con token; credenciales incorrectas → 401 |

## Regla de Negocio: Cálculo de Tarifas

El método `EnvioService.calcularTarifa(pesoKg, distanciaKm)` aplica:

```
tarifa = tarifa base (₡1000)
       + componente de peso
       + distancia (km) × ₡100

componente de peso:
  - hasta 50 kg        → peso × ₡100
  - excedente sobre 50 → 50 × ₡100 + (peso − 50) × ₡115   (recargo del 15% por carga pesada)
```

Validado con `@ParameterizedTest` + `@CsvSource` para los casos (5 kg, 10 km) → ₡2500,
(15 kg, 50 km) → ₡7500 y (100 kg, 2.5 km) → ₡12000.

## Ejecutar las Pruebas

```bash
cd backend

# Solo ejecutar las pruebas
mvn clean test

# Ejecutar pruebas + generar reporte + verificar el umbral del 85%
mvn clean verify
```

`mvn clean verify` debe finalizar con **BUILD SUCCESS**. Si la cobertura de instrucciones del
paquete `cr.ac.ucr.paraiso.ie.c5k023.lab06.business` baja del 85%, la meta `jacoco:check`
falla la construcción intencionalmente.

> Las pruebas **no requieren SQL Server**: todas las dependencias de persistencia están
> sustituidas por mocks y los controladores se prueban con `@WebMvcTest` sin levantar el
> contexto completo de la aplicación.

## Ver el Reporte de Cobertura

Después de `mvn clean verify`, abrí en el navegador:

```
backend/target/site/jacoco/index.html
```

El reporte muestra el porcentaje de instrucciones y ramas cubiertas por paquete y por clase.

---

## Autor

Juan Pablo Solano Vásquez — Carné C5K023