# ExpresoFast — Laboratorios 6 al 9 (Seguridad JWT/RBAC, Pruebas y Paginación)

**Universidad de Costa Rica — Sede del Atlántico, Recinto Paraíso**
**Carrera de Informática Empresarial**
**Curso:** IF0009 — Desarrollo de Software IV
**Profesor:** Mag. Jonathan Granados C.
**Semestre:** II-2026

**Estudiante:** Juan Pablo Solano Vásquez
**Carné:** C5K023

> **Laboratorio 7** (suite de pruebas con JUnit 5, Mockito, MockMvc y JaCoCo) se documenta en la
> sección *Laboratorio 7* de este archivo.
> **Laboratorio 9** (paginación en servidor, Stored Procedures y dashboard paginado) se desarrolla
> en la rama `lab09`. Ver la sección *Laboratorio 9* más abajo.

---

## Descripción

Plataforma full-stack de logística y monitoreo de envíos express. Extiende el proyecto del
Laboratorio 5 con autenticación JWT, control de acceso por roles (RBAC), DTOs con validación
estricta, manejo centralizado de excepciones, bitácora de auditoría de cambios de estado, una
suite de pruebas automatizadas (Lab 7) y, en el Laboratorio 9, paginación en el servidor,
consultas mediante Stored Procedures y un dashboard paginado.

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
│   ├── 04_schema_lab7_extension.sql
│   ├── 05_schema_lab9_extension.sql
│   └── 06_data_lab9_seeds.sql
├── frontend/                         # HTML5 + CSS3 + JS vanilla
│   ├── index.html                    # Inicio de sesión
│   ├── dashboard.html                # Tablero de envíos (Lab 6)
│   ├── dashboard_paginado.html       # Tabla paginada (Lab 9)
│   ├── styles.css
│   ├── app.js                        # Sesión, JWT y tablero
│   └── paginado.js                   # Paginación y consultas por SP (Lab 9)
├── docs/
│   └── ExpresoFast_Postman_Collection.json
└── README.md
```

## Guía de Configuración de Base de Datos

1. Abrí SSMS y conectate a tu instancia local de SQL Server.
2. Ejecutá los 6 scripts de la carpeta `database/`, **en orden**:
   - `01_schema_lab5.sql` — crea la base de datos `ExpresoFastC5K023_II2026` y las tablas del dominio logístico (`EmpresaLogistica`, `Vehiculo`, `Conductor`, `Envio`).
   - `02_schema_lab6_extension.sql` — agrega las tablas de seguridad y auditoría (`Usuario`, `Rol`, `UsuarioRol`, `BitacoraEnvio`).
   - `03_data_seeds.sql` — inserta datos de prueba: 1 empresa, 3 vehículos, 2 conductores, los 3 roles del sistema y 3 usuarios de prueba con contraseñas ya encriptadas en BCrypt.
   - `04_schema_lab7_extension.sql` — **(Laboratorio 7)** agrega la columna `activo` a `Conductor` y `conductor_asignado_id` a `Vehiculo`, necesarias para las reglas de negocio de asignación de conductores.
   - `05_schema_lab9_extension.sql` — **(Laboratorio 9)** agrega la columna `destinatario` a `Envio` y crea los Stored Procedures `SP_OBTENER_ENVIOS_POR_ESTADO` (parámetro `@pEstado`) y `SP_RESUMEN_METRICAS_ENVIOS`. Es idempotente.
   - `06_data_lab9_seeds.sql` — **(Laboratorio 9)** completa el `destinatario` de los envíos existentes e inserta envíos adicionales hasta superar los 15 registros, con los 4 estados representados, para poder probar la paginación.

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
| `/api/envios/{id}` | GET | ADMIN, OPERADOR, CONDUCTOR |
| `/api/envios` | POST | ADMIN, OPERADOR |
| `/api/envios/{id}/estado` | PATCH | ADMIN, OPERADOR, CONDUCTOR |
| `/api/envios/{id}/cancelar` | POST | ADMIN, OPERADOR |
| `/api/envios/{id}/bitacora` | GET | ADMIN, OPERADOR |
| `/api/v1/envios/**` | GET | ADMIN, OPERADOR, CONDUCTOR |
| `/api/vehiculos/**` | Todos | ADMIN |

## Instrucciones de Ejecución

### Backend

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Frontend

El frontend es HTML/CSS/JS estático, sin build. Abrí `frontend/index.html` con una extensión
tipo **Live Server** (VS Code) o cualquier servidor estático local — no lo abras con doble clic
directo desde el explorador de archivos, porque las peticiones `fetch` requieren que se sirva
por HTTP, no por `file://`.

1. Iniciá el backend primero.
2. Abrí `frontend/index.html` (pantalla de inicio de sesión).
3. Iniciá sesión con cualquiera de los usuarios de prueba de la tabla anterior. Al ingresar se abre `dashboard.html`.
4. Desde el tablero, el enlace **Vista paginada** lleva a `dashboard_paginado.html` (Laboratorio 9).

El frontend consume la API en `http://localhost:8080/api` (constante `API_BASE` en `app.js`) y
guarda el token JWT en `sessionStorage`.

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

# Laboratorio 9 — Paginación, Stored Procedures y Dashboard Paginado

## Endpoints (`/api/v1/envios`)

Requieren el header `Authorization: Bearer <token>` (roles ADMIN, OPERADOR o CONDUCTOR).

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/envios` | Lista paginada de envíos (`Page<EnvioDTO>`). |
| GET | `/api/v1/envios/procedimiento/{estado}` | Envíos por estado, consultados con el Stored Procedure `SP_OBTENER_ENVIOS_POR_ESTADO`. `estado`: `PENDIENTE`, `EN_TRANSITO`, `ENTREGADO` o `CANCELADO`. |

Parámetros de `GET /api/v1/envios`:

| Parámetro | Por defecto | Descripción |
|---|---|---|
| `page` | `0` | Número de página, **base 0**. |
| `size` | `5` | Registros por página. |
| `sortBy` | `fechaCreacion` | Campo de ordenamiento. |
| `direction` | `desc` | `asc` o `desc`. |
| `busqueda` | — | Texto libre de búsqueda. |
| `estado` | — | Filtra por estado. |

Ejemplo:

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/envios?page=2&size=5"
```

Spring Data pagina desde 0, por lo que la interfaz muestra "Página 3" cuando envía `page=2`.

## Dashboard paginado

`frontend/dashboard_paginado.html` con su script `frontend/paginado.js`:

- Tabla de guías de envío con paginador: primera, anterior, siguiente y última página.
- Indicador "Página X de Y (Total: N envíos)".
- Selector de tamaño de página (5, 10 o 20) y campo de búsqueda.
- Selector de consulta: *Todos los envíos (paginado)* o una consulta por Stored Procedure por cada estado. En este modo la tabla no se pagina y se ve la línea del procedimiento en la terminal del backend.

Requiere haber iniciado sesión en `index.html`: sin token, la página redirige al inicio de sesión.

## Verificación

1. Con el backend y Live Server en ejecución, abrir `dashboard_paginado.html`.
2. Navegar hasta que la pantalla diga "Página 3" y comprobar en DevTools → Network que la petición lleva `page=2&size=5`.
3. Elegir una opción *Stored Procedure* y verificar en la terminal del backend la llamada a `SP_OBTENER_ENVIOS_POR_ESTADO`.
4. Confirmar que la terminal no muestra advertencias `HHH000104`.

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