# AppFinanzas - Control de Ingresos, Gastos y Cuentas Financieras
### Avance de Proyecto Final 3 (APF3) - Desarrollo Web Integrado (UTP)

Este proyecto ha sido organizado en una arquitectura desacoplada y modular con carpetas independientes para el **Frontend** y el **Backend**:

```
AppFinanzas/
├── backend/                       # Proyecto Backend en Spring Boot 3 (Java 17)
│   ├── src/                       # Controladores REST, Entidades, Seguridad y Repositorios
│   ├── build.gradle               # Dependencias de Gradle (Web, JPA, Security, JWT, H2, Postgres)
│   ├── gradlew.bat                # Wrapper de ejecución de Gradle
│   ├── .env                       # Variables de entorno y credenciales (H2 / Postgres)
│   └── compose.yaml               # Docker Compose para PostgreSQL opcional
├── frontend/                      # Proyecto Frontend en Angular 20 Standalone
│   ├── src/                       # Componentes, Formularios, Servicios e Interceptores
│   ├── package.json               # Dependencias de Angular y scripts
│   └── angular.json               # Configuración del CLI de Angular
├── Informacion_proyecto/          # Diapositivas de clase (S11 a S14) y plantillas
│   └── Excel_Avance3.xlsx         # Matriz oficial llenada con las 10 operaciones REST y formularios
├── DOCUMENTACION_APF3.md          # Documentación técnica completa del proyecto
└── 262UWIN-AVANCE3_AppFinanzas.xlsx # Matriz Excel complementaria de entrega
```

---

## 🚀 Cómo Iniciar el Proyecto

### 1. Iniciar el Backend (Spring Boot en puerto 8081)
Abre una terminal en la carpeta `backend`:
```powershell
cd backend
.\gradlew.bat bootRun
```
*El backend se ejecutará en: `http://localhost:8081` (con base de datos H2 en memoria y datos demo precargados).*

### 2. Iniciar el Frontend (Angular en puerto 4200)
Abre otra terminal en la carpeta `frontend`:
```powershell
cd frontend
npm start
```
*El frontend estará disponible en: **`http://localhost:4200`**.*

---

## 🔐 Credenciales de Demostración
- **Usuario:** `ruta-user`
- **Contraseña:** `secreto123`
*(O puedes registrar un nuevo usuario desde la pantalla de login con el Formulario 2).*
