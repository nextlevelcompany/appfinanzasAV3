# AppFinanzas API

API REST de finanzas personales con Spring Boot, Spring Data JPA, PostgreSQL y JWT.

## Preparación

La carpeta incluye `compose.yaml` para crear una instancia PostgreSQL independiente en Docker, con volumen persistente y puerto local 5434. Crea un `.env` local con claves seguras, inicia la base y después la API:

```powershell
$pgPass = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$jwtKey = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
@"
POSTGRES_DB=appfinanzas
POSTGRES_USER=appfinanzas
POSTGRES_PASSWORD=$pgPass
DB_URL=jdbc:postgresql://localhost:5434/appfinanzas
DB_USERNAME=appfinanzas
DB_PASSWORD=$pgPass
JWT_SECRET_BASE64=$jwtKey
"@ | Set-Content -Encoding utf8 .env
docker compose up -d db adminer
./gradlew.bat bootRun
```

La página de inicio queda disponible en `http://localhost:8081/`. La base usa el volumen `appfinanzas-postgres-data`, por lo que sus datos permanecen después de reiniciar el contenedor. Para detener PostgreSQL sin borrar los datos:

Adminer proporciona una interfaz visual en `http://localhost:8082/`. Inicia sesión con sistema `PostgreSQL`, servidor `db`, base `appfinanzas`, usuario `appfinanzas` y la contraseña `DB_PASSWORD` guardada localmente en `.env`.

```powershell
docker compose down
```

Hibernate crea o actualiza las tablas al iniciar (`spring.jpa.hibernate.ddl-auto=update`). Las credenciales y la clave JWT se leen del entorno; no se guardan en el repositorio.

## Autenticación

1. `POST /auth/register` con `{"username":"ana","password":"clave1234"}`. Las contraseñas se guardan con BCrypt.
2. `POST /auth/login` con las mismas credenciales. La respuesta contiene `token`, `tokenType` y `username`.
3. En las rutas `/api/**`, envía `Authorization: Bearer <token>`.

El acceso a cuentas y transacciones requiere JWT. El filtro valida la firma y expiración del token y vuelve a consultar el usuario en PostgreSQL.

## Operaciones REST

Todas las rutas `/api/**` requieren el encabezado `Authorization: Bearer <token>`.

| Método | Ruta | Uso |
| --- | --- | --- |
| POST | `/api/cuentas` | Crear cuenta |
| GET | `/api/cuentas` | Listar cuentas |
| GET | `/api/cuentas/{id}` | Consultar cuenta |
| PUT | `/api/cuentas/{id}` | Actualizar cuenta |
| DELETE | `/api/cuentas/{id}` | Eliminar cuenta |
| POST | `/api/cuentas/categorias` | Crear categoría |
| GET | `/api/cuentas/categorias` | Listar categorías |
| GET | `/api/cuentas/resumen` | Consultar totales y balance |
| POST | `/api/transacciones` | Crear transacción |
| GET | `/api/transacciones` | Listar transacciones |
| GET | `/api/transacciones/{id}` | Consultar transacción |
| GET | `/api/transacciones/tipo/{tipo}` | Filtrar por `INGRESO` o `GASTO` |
| GET | `/api/transacciones/categoria/{nombre}` | Buscar por categoría |
| GET | `/api/transacciones/rango?desde=2026-09-01&hasta=2026-09-30` | Filtrar por fechas |
| PUT | `/api/transacciones/{id}` | Actualizar transacción |
| DELETE | `/api/transacciones/{id}` | Eliminar transacción |

Las entidades persistentes son `Cuenta`, `Transaccion`, `Categoria` y `Usuario`. Los repositorios extienden `JpaRepository`; se usan consultas derivadas para filtros y JPQL para el resumen.

## Casos de prueba

`ControlFinanzasTest` describe entradas, resultados esperados y verificaciones para los controladores de transacciones, cuentas/categorías y autenticación. Las pruebas usan H2 en memoria y no requieren una instancia de PostgreSQL.

```powershell
./gradlew.bat test
```
