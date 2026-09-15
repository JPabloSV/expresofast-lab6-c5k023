# ExpresoFast — Laboratorio 6 (Parte II: Seguridad JWT, RBAC, DTOs y Bitácora)

**Universidad de Costa Rica — Sede del Atlántico, Recinto Paraíso**
**Carrera de Informática Empresarial**
**Curso:** IF0009 — Desarrollo de Software IV
**Profesor:** Mag. Jonathan Granados C.
**Semestre:** II-2026

**Estudiante:** Juan Pablo Solano Vásquez
**Carné:** C5K023

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
│   └── 03_data_seeds.sql
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

## Autor

Juan Pablo Solano Vásquez — Carné C5K023