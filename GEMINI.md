# ARA: Ambulance Routing Application - Project Instructions

## 🚀 Project Overview
The Ambulance Routing Application (ARA) is a high-performance emergency response management system designed for Panabo City. It optimizes ambulance dispatch and routing using a custom implementation of the **Bellman-Ford Algorithm**, which accounts for dynamic traffic conditions simulated via negative edge weights.

### 🏗 Architecture
- **Backend**: Spring Boot (Java 17) providing a RESTful API and the core routing engine.
- **Frontend**: Vue 3 (Composition API) with Vite, Vue Router, and Leaflet for map visualization.
- **Algorithm**: Bellman-Ford (supports negative weights and cycle detection).
- **Database**: MySQL (Standardized relational schema).

---

## 📂 Project Structure

### Backend (`/`)
- `src/main/java/com/example/bellmanford/`
    - `controller/`: REST API endpoints (`AuthController`, `MissionController`, `RouteController`, `FleetController`).
    - `model/`: JPA Entities (`User`, `Ambulance`, `Hospital`, `Location`, `Patient`, `RequestLog`, `GeneratedPath`).
    - `service/`: Business logic (`BellmanFordService`, `MissionService`, `MockDatabaseService`).
    - `repository/`: Spring Data JPA interfaces.
- `src/main/resources/`
    - `application.properties`: DB configuration.
    - `osm_data.json`: OpenStreetMap data used for graph construction.

### Frontend (`/frontend/`)
- `src/views/`: Main page components (`Login.vue`, `DispatcherDashboard.vue`, `DriverDashboard.vue`, `MissionLogs.vue`, `Profile.vue`).
- `src/components/`: Reusable components (`TopNav.vue`, `Map.vue`).
- `src/composables/`: Reactive logic (`useAuth.js`).
- `src/router/`: Vue Router configuration with navigation guards.

---

## 🛠 Building and Running

### Backend (Spring Boot)
- **Install Dependencies**: `mvn clean install`
- **Run Application**: `mvn spring-boot:run`
- **Database**: Ensure MySQL is running. The app uses `ara_db` by default.

### Frontend (Vue.js)
- **Navigate**: `cd frontend`
- **Install Dependencies**: `npm install --legacy-peer-deps`
- **Run Dev Server**: `npm run dev` (Runs on `http://localhost:5173`)
- **Build for Production**: `npm run build`

---

## 🧠 Development Conventions

### ⚙️ Backend Standards
- **Standardized Schema**: Always use the normalized relational schema (refer to `RequestLog`, `Location`, `Hospital`). Avoid denormalization.
- **Services**: Business logic must reside in `@Service` classes, not controllers.
- **Error Handling**: Use `ResponseEntity` with appropriate status codes (e.g., 401 for Auth, 404 for missing entities).

### 🎨 Frontend Standards
- **Vue 3 Composition API**: Use `<script setup>` for all new components.
- **Reactive State**: Prefer `ref` and `computed` for state management.
- **Map Visualization**: Maintain manual Leaflet layer management within `Map.vue` to ensure performance during algorithm visualization.
- **Styling**: Adhere to the established `App.css` variables and Tailwind-like utility patterns.

### 🔄 Mission Flow
1. Dispatcher creates a `Mission` (DTO).
2. `MissionService` validates the request and creates a standardized `RequestLog` (Entity).
3. The `RequestLog` acts as both the active mission and the permanent audit log.
4. Driver dashboard polls for active missions via `GET /api/missions/active/{driverId}` and updates status via `PUT /api/missions/{id}/status`.
5. Status transitions update both the request log and the ambulance fleet status.
