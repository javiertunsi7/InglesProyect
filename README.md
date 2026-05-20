# Aprende Inglés (General + Tech) por niveles CEFR

Plataforma full-stack para practicar inglés en dos categorías —**General** y **Técnico para programadores**— con ejercicios organizados por nivel CEFR (A1 → C2). El feedback siempre llega en español.

> **Stack:** Spring Boot 3.4 + Java 21 (backend) · React 18 + Vite 6 (frontend) · H2 in-memory (BD dev)

---

## Tabla de contenidos

1. [Demo rápida](#demo-rápida)
2. [Arquitectura](#arquitectura)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Requisitos](#requisitos)
5. [Cómo arrancar](#cómo-arrancar)
6. [API REST](#api-rest)
7. [Cómo escalar el proyecto](#cómo-escalar-el-proyecto)
8. [Decisiones de diseño](#decisiones-de-diseño)

---

## Demo rápida

Hay una **demo standalone** en [`demo/index.html`](demo/index.html) que muestra el flujo completo sin necesidad de arrancar backend ni frontend:

- Abre el archivo directamente en el navegador, o
- Sírvelo con cualquier servidor estático: `npx serve demo`

La demo replica los mismos datos y la misma UI que la app real, pero con datos mockeados en JS (sin Spring Boot).

> 📺 Si lo abres dentro de Claude Code, el archivo aparece automáticamente en el panel de **Launch preview**.

---

## Arquitectura

### Vista general (ASCII)

```
┌────────────┐        HTTP :5173        ┌──────────────────────────────┐
│  Usuario   │ ───────────────────────▶ │ FRONTEND (React + Vite)      │
│ Navegador  │                          │  ├── Pages                   │
└────────────┘                          │  ├── Components              │
                                        │  ├── Hooks (useFetch)        │
                                        │  ├── Services                │
                                        │  └── apiClient (axios)       │
                                        └──────────────┬───────────────┘
                                                       │ REST /api/v1
                                          (Vite proxy)│ JSON
                                                       ▼
                                        ┌──────────────────────────────┐
                                        │ BACKEND (Spring Boot)        │
                                        │  ├── @RestController         │
                                        │  ├── @Service                │
                                        │  ├── Mappers (Entity↔DTO)    │
                                        │  ├── @Repository (JPA)       │
                                        │  └── @Entity (model)         │
                                        │                              │
                                        │  + GlobalExceptionHandler    │
                                        │  + CORS WebConfig            │
                                        │  + DataInitializer (seed)    │
                                        └──────────────┬───────────────┘
                                                       │ JPA / Hibernate
                                                       ▼
                                        ┌──────────────────────────────┐
                                        │ H2 (in-memory database)      │
                                        │  categories · levels         │
                                        │  exercises · exercise_options│
                                        └──────────────────────────────┘
```

### Vista completa (draw.io)

El diagrama detallado está en [`docs/architecture.drawio`](docs/architecture.drawio).

**Cómo abrirlo:**
- Online: ve a https://app.diagrams.net/ → `File → Open from → Device` → selecciona el archivo.
- VS Code: instala la extensión **"Draw.io Integration"** y el archivo se abre directamente.
- Desktop: descarga la app de https://www.drawio.com/.

---

## Estructura del proyecto

```
InglesProyect/
├── backend/                                  Spring Boot 3 + Java 21
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/englishlearning/
│       │   ├── EnglishLearningApplication.java
│       │   ├── config/                       CORS + carga inicial de datos
│       │   │   ├── WebConfig.java
│       │   │   └── DataInitializer.java
│       │   ├── controller/                   endpoints REST (/api/v1/...)
│       │   ├── service/                      lógica de negocio
│       │   ├── repository/                   interfaces JPA
│       │   ├── domain/
│       │   │   ├── enums/                    LevelCode · CategoryType · ExerciseType
│       │   │   └── model/                    entidades JPA
│       │   ├── dto/                          records (transporte)
│       │   ├── mapper/                       Entity ↔ DTO
│       │   └── exception/                    404 · 400 · 500 (en español)
│       └── resources/
│           └── application.properties
│
├── frontend/                                 React 18 + Vite 6
│   ├── package.json
│   ├── vite.config.js                        proxy /api → backend
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx                           router
│       ├── api/apiClient.js                  axios centralizado
│       ├── services/                         categoryService · levelService · exerciseService
│       ├── hooks/useFetch.js                 hook genérico loading/error/data
│       ├── components/                       Header · CategoryCard · LevelCard · ExerciseCard
│       ├── pages/                            HomePage · LevelSelectionPage · ExercisePage · NotFoundPage
│       └── styles/global.css
│
├── demo/
│   └── index.html                            demo standalone (sin backend)
│
├── docs/
│   └── architecture.drawio                   diagrama de arquitectura
│
├── README.md
└── .gitignore
```

---

## Requisitos

| Herramienta | Versión mínima | Para qué |
|-------------|----------------|----------|
| Java | 21 | Backend |
| Maven | 3.9 | Build Spring Boot |
| Node.js | 18 | Frontend |
| npm | 9 | Gestor de paquetes |

Comprobación rápida:

```powershell
java -version
mvn -v
node -v
npm -v
```

---

## Cómo arrancar

### 1. Backend (puerto 8080)

```powershell
cd backend
mvn spring-boot:run
```

- API: `http://localhost:8080/api/v1/...`
- Consola H2 (visualiza la BD): `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:englishdb`
  - Usuario: `sa` · Contraseña: *(vacía)*

### 2. Frontend (puerto 5173)

```powershell
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite hace **proxy automático** de `/api` → backend, así que el frontend no necesita conocer la URL absoluta.

### 3. Demo (sin servidor)

```powershell
cd demo
npx serve .
# o simplemente: abre demo/index.html en el navegador
```

---

## API REST

Todos los endpoints están bajo `/api/v1`.

| Método | URL | Devuelve |
|--------|-----|----------|
| `GET`  | `/api/v1/categories` | Lista de categorías (GENERAL/TECH) |
| `GET`  | `/api/v1/categories/{TYPE}/levels` | Niveles A1..C2 de esa categoría |
| `GET`  | `/api/v1/categories/{TYPE}/levels/{CODE}/exercises` | Ejercicios del nivel |
| `POST` | `/api/v1/exercises/{id}/answer` | Resultado + explicación |

`TYPE` ∈ `{ GENERAL, TECH }` · `CODE` ∈ `{ A1, A2, B1, B2, C1, C2 }`

**Ejemplo POST de respuesta:**

```http
POST /api/v1/exercises/1/answer
Content-Type: application/json

{ "answer": "am" }
```

```json
{
  "correct": true,
  "message": "¡Correcto! Muy bien hecho.",
  "correctAnswer": "am",
  "explanation": "Con el pronombre 'I' se usa siempre 'am' en presente."
}
```

---

## Cómo escalar el proyecto

| Quieres... | Dónde tocar |
|------------|-------------|
| **Más ejercicios** | `DataInitializer.java` → añade entradas a `GENERAL_EXERCISES` / `TECH_EXERCISES` |
| **BD persistente** | `application.properties` → cambia `spring.datasource.*` a PostgreSQL/MySQL |
| **Usuarios y progreso** | Nuevas entities `User` + `UserProgress` + endpoints `/users/{id}/progress` + login en frontend |
| **Nuevo tipo de ejercicio** | Añade valor al enum `ExerciseType` + maneja el render en `ExerciseCard.jsx` |
| **Nueva categoría (ej: BUSINESS)** | Añade `BUSINESS` al enum `CategoryType` + entradas en `DataInitializer` — el resto del código ya lo soporta |
| **Tests** | Añade JUnit 5 + MockMvc en `backend/src/test/...` y Vitest + Testing Library en `frontend` |
| **Despliegue** | Backend: `mvn package` → ejecuta el JAR · Frontend: `npm run build` → sirve `dist/` desde nginx o el propio Spring Boot |

---

## Decisiones de diseño

- **Modelo plano (sin `@OneToMany`)**: las entidades guardan IDs foráneos como `Long` en vez de relaciones. Evita el infierno del lazy loading al serializar JSON y mantiene los DTOs limpios.
- **Records de Java para DTOs**: inmutabilidad gratis, una línea por DTO.
- **`@RequiredArgsConstructor` (Lombok)**: inyección de dependencias por constructor (mejor que `@Autowired` en campos).
- **Hook `useFetch`**: centraliza el patrón loading/error/data en el frontend y evita repetir `useEffect` en cada página.
- **Sin librería UI**: solo CSS variables y BEM ligero. Fácil de migrar a Tailwind / Material UI más tarde.
- **Código en inglés, mensajes en español**: nombres de clases, variables y endpoints en inglés (estándar de la industria); cualquier texto que vea el usuario está en español.
- **Versionado de API (`/v1`)**: permite evolucionar el backend sin romper clientes existentes.
- **`correctAnswer` no se expone** en `ExerciseResponse`: solo viaja en la respuesta tras enviar la respuesta del usuario. Evita filtrar las soluciones por la API.
