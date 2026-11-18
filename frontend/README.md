# Frontend AppFinanzas - Angular 20 (Avance de Proyecto Final 3)

Este proyecto corresponde al Frontend de **AppFinanzas** desarrollado en **Angular 20** (componentes standalone, inyección con `inject()`, señales `signal`, formularios reactivos y por plantilla, y consumo de servicios REST con `provideHttpClient`).

---

## 📋 Requisitos Previos y Ejecución

### 1. Iniciar el Backend (Spring Boot)
En la raíz del proyecto (`AppFinanzas/`):
```bash
./gradlew bootRun
```
El backend se levantará en: `http://localhost:8081`

### 2. Iniciar el Frontend (Angular)
Desde la carpeta `frontend/`:
```bash
npm start
# o: npx ng serve --port 4200
```
La aplicación web estará disponible en: **`http://localhost:4200`**

---

## 🏆 Cumplimiento de la Rúbrica de Calificación (APF3)

### 1. Operaciones REST (8 / 8 Puntos)
> *Se han expuesto al menos 8 operaciones REST en al menos dos controladores. Las operaciones son de utilidad para el proyecto. Las operaciones son invocadas desde Angular.*

El sistema cuenta con **17 operaciones REST** expuestas e invocadas desde servicios Angular:
- **Controlador 1 (`RestCuentaController` - `/api/cuentas`):**
  1. `POST /api/cuentas` - Crear cuenta financiera.
  2. `GET /api/cuentas` - Listar todas las cuentas.
  3. `GET /api/cuentas/{id}` - Consultar cuenta por ID.
  4. `PUT /api/cuentas/{id}` - Modificar cuenta por ID.
  5. `DELETE /api/cuentas/{id}` - Eliminar cuenta por ID.
  6. `POST /api/cuentas/categorias` - Registrar categoría.
  7. `GET /api/cuentas/categorias` - Listar categorías.
  8. `GET /api/cuentas/resumen` - Obtener resumen financiero (ingresos, gastos, balance, estado).
- **Controlador 2 (`RestTransaccionController` - `/api/transacciones`):**
  9. `POST /api/transacciones` - Registrar transacción.
  10. `GET /api/transacciones` - Listar historial de transacciones.
  11. `GET /api/transacciones/{id}` - Consultar transacción por ID.
  12. `DELETE /api/transacciones/{id}` - Eliminar transacción por ID.
  13. `GET /api/transacciones/tipo/{tipo}` - Filtrar transacciones por INGRESO o GASTO.
  14. `GET /api/transacciones/categoria/{nombre}` - Filtrar por categoría.
  15. `GET /api/transacciones/rango` - Filtrar por rango de fechas (desde, hasta).
- **Controlador de Seguridad (`AuthController` - `/auth`):**
  16. `POST /auth/login` - Inicio de sesión y emisión de token JWT.
  17. `POST /auth/register` - Registro de usuario con BCrypt.

---

### 2. Formularios Angular (8 / 8 Puntos)
> *Se han implementado al menos 6 formularios en funcionamiento con Angular.*

Se implementaron **8 formularios en funcionamiento**, abarcando ambos enfoques enseñados en clase:
1. **Formulario 1 (Template-driven / FormsModule):** Login de Usuario (`/login`).
2. **Formulario 2 (Reactivo / ReactiveFormsModule):** Registro de Usuario con validaciones (`/login`).
3. **Formulario 3 (Reactivo / ReactiveFormsModule):** Registro de Cuenta Financiera (`/cuentas`).
4. **Formulario 4 (Template-driven / FormsModule):** Edición / Actualización de Cuenta (`/cuentas`).
5. **Formulario 5 (Reactivo / ReactiveFormsModule):** Registro de Transacción (`/transacciones`).
6. **Formulario 6 (Template-driven / FormsModule):** Filtro por Rango de Fechas (`/transacciones`).
7. **Formulario 7 (Reactivo / ReactiveFormsModule):** Filtro por Tipo y Categoría (`/transacciones`).
8. **Formulario 8 (Template-driven / FormsModule):** Registro de Categoría (`/categorias`).

---

### 3. Presentación (4 / 4 Puntos)
> *La presentación se ha realizado sin ningún inconveniente/problema. La presentación ha sido clara.*

- Interfaz moderna, limpia y responsiva diseñada con **Bootstrap 5.3** y Bootstrap Icons.
- Barra de navegación con estado de autenticación dinámico.
- Interceptor HTTP funcional (`authInterceptor`) que adjunta automáticamente el token Bearer en cada petición protegida.
- Vista interactiva `/operaciones-rest` que documenta la matriz requerida y permite probar las peticiones en vivo frente al docente.
- Archivo Excel generado: `262UWIN-AVANCE3_AppFinanzas.xlsx`.
