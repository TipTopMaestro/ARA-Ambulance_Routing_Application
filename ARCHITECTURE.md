# ARA: Ambulance Routing Application - Technical Architecture

The Ambulance Routing Application (ARA) is a stateful, role-based system designed for emergency dispatch and navigation in Panabo City. It combines a Spring Boot backend with a React frontend, featuring a custom implementation of the Bellman-Ford algorithm to handle complex routing scenarios.

---

## 📂 Project Structure

### 🏗 Backend (Java / Spring Boot)
Located in `src/main/java/com/example/bellmanford/`
*   **`/controller`**: REST API Endpoints.
    *   `AuthController`: Login and session validation.
    *   `FleetController`: Monitoring ambulance status and hospital stations.
    *   `MissionController`: Managing the lifecycle of a dispatch mission.
    *   `RouteController`: Fetching map nodes and initiating path calculations.
*   **`/service`**: Core Business Logic.
    *   `BellmanFordService`: The routing engine. Implements shortest-path logic, relaxation tracking for visualization, and negative cycle detection.
    *   `MissionService`: Orchestrates the transition from incident to active mission and log archival.
    *   `MockDatabaseService`: Graph constructor. Parses OSM data, manages aliases (e.g., "H1"), seeds initial data, and injects random negative weights.
*   **`/model`**: JPA Entities & DTOs.
    *   `User`, `Ambulance`, `Hospital`, `Location`, `Patient`, `RequestLog`: Standardized relational entities persisted in MySQL.
    *   `GraphNode`, `GraphEdge`: In-memory representation of the city map.
    *   `RelaxationStep`, `PathResponse`: Data structures for frontend visualization.
*   **`/repository`**: Spring Data JPA interfaces for MySQL access.

### 💻 Frontend (Vue 3 / Vite)
Located in `frontend/src/`
*   **`/views`**:
    *   `Login.vue`: Role-based authentication entry point.
    *   `DispatcherDashboard.vue`: Complex UI for incident entry, fleet monitoring, and algorithm visualization.
    *   `DriverDashboard.vue`: Mission-focused UI for navigation and status updates.
    *   `MissionLogs.vue`: Historical audit and analysis view.
*   **`/components`**:
    *   `Map.vue`: High-performance Leaflet integration with custom layers for negative edges and real-time relaxation.
*   **`/composables`**: Reactive logic for auth and state management.

---

## 🧠 Core Logic & Algorithm

### 1. The Graph (Nodes & Edges)
*   **Nodes:** Extracted from OpenStreetMap (OSM) JSON. Significant nodes (intersections, turns, hospitals) are converted into `GraphNode` objects.
*   **Edges:** Road segments connecting nodes.
    *   **Base Weights:** Calculated using the Haversine formula (distance) multiplied by a travel time constant.
    *   **Negative Weights:** To test Bellman-Ford's robustness, 5% of edges are randomly assigned negative values (`-1.0` to `-6.0`), simulating high-priority corridors or simulated shortcuts.

### 2. Bellman-Ford Routing
Unlike Dijkstra, this implementation handles negative edge weights:
*   **Relaxation:** The service iterates $V-1$ times through all edges. If a shorter path is found via an edge $(u, v)$, the distance to $v$ is updated.
*   **Visualization Data:** Every time a distance is updated, a `RelaxationStep` is recorded. This allows the frontend to replay the algorithm's "thinking process."
*   **Negative Cycle Detection:** A $V$-th iteration checks if distances can still be reduced. If so, a negative cycle exists, and a warning is issued.

---

## 🔄 Mission Lifecycle & Flow

1.  **Authentication:** User logs in ➔ `AuthContext` stores the role (Dispatcher/Driver).
2.  **Incident Logging:** Dispatcher selects a Hospital (Source) and Patient Location (Destination).
3.  **Unit Assignment:** Picking a hospital triggers a modal showing only **Available** ambulances at that specific station. Selecting one moves it to **Reserved** status.
4.  **Route Calculation:**
    *   Frontend calls `POST /api/path`.
    *   Backend re-runs Bellman-Ford and returns the shortest path + 1000s of relaxation steps.
    *   Frontend "replays" the relaxation (Amber lines) and then draws the final path (**Green line**).
5.  **Dispatch:** Dispatcher clicks "Dispatch". Mission is saved to MySQL; status is set to `DISPATCHED`.
6.  **Driver Receipt:** Driver dashboard polls `GET /api/missions/active/{id}`. The mission and navigation map appear instantly.
7.  **Status Progression:** Driver updates status: `EN_ROUTE` ➔ `TRANSPORT` ➔ `COMPLETED`.
8.  **Automatic Reset:** Upon `COMPLETED`, the Mission is archived, the Ambulance returns to `AVAILABLE`, and both dashboards clear for the next emergency.

---

## 📡 API Communication Patterns

| Endpoint | Method | Purpose |
| :--- | :--- | :--- |
| `/api/auth/login` | `POST` | Validates credentials and returns user role. |
| `/api/path` | `POST` | Runs Bellman-Ford; returns path, relaxation steps, and negative cycle status. |
| `/api/nodes` | `GET` | Fetches all significant OSM nodes for map rendering. |
| `/api/ambulances` | `GET` | Returns status of the entire fleet. |
| `/api/missions` | `POST` | Creates a new mission and reserves the ambulance. |
| `/api/missions/active/{id}`| `GET` | Fetches the current non-completed mission for a specific driver. |
| `/api/missions/{id}/status`| `PUT` | Updates mission status and triggers ambulance state transitions. |

---

## 🗺 Map Visualization Layers

*   **Base Layer:** OpenStreetMap tiles.
*   **Node Layer:** Circle markers (Red=Hospital, Amber=Ambulance, Blue=Standard, Green=Selection).
*   **Negative Edge Layer:** **Purple dashed lines** with tooltips showing specific negative weights.
*   **Relaxation Layer:** **Bright Amber thick lines** that flash in real-time during route generation.
*   **Optimal Path Layer:** **Solid Green thick line** showing the final calculated route.
