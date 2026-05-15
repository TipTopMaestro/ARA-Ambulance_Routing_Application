# ARA: Ambulance Routing Application

ARA is a state-of-the-art emergency response management system designed to optimize ambulance dispatch and routing for Panabo City. It leverages a custom **Bellman-Ford Algorithm** implementation to calculate shortest paths while accounting for dynamic factors like traffic anomalies (simulated via negative edge weights).

## 🚀 Key Features

### 📡 Unified Dashboards & UI
- **Modern Layout**: A 3-column Dispatcher Dashboard (Form, Map, Status) with a persistent Top Navigation bar.
- **Role-Based Access**: Specialized views for Dispatchers (Incident Entry, Route Generation) and Drivers (Navigation, Status Progression).
- **Mission Logs**: A dedicated view for historical analysis and auditing of emergency responses, unified with active mission tracking.

### 🚒 Dynamic Routing & Visualization
- **Bellman-Ford Algorithm**: Handles complex graphs with negative weights (shortcuts/priority corridors) and detects traffic cycles.
- **Real-Time Visualization**: Watch the algorithm's "thinking process" as it relaxes edges across the city map.
- **Interactive Map**: High-performance Leaflet integration with custom layers for optimal paths and traffic anomalies.

### 📊 Standardized Data Architecture
- **Normalized Schema**: Robust relational database structure ensuring data integrity for Patients, Hospitals, Fleet, and standardized Request Logs.
- **Route Archiving**: Every generated optimal path is cached and logged for post-incident review.

---

## 🛠 Tech Stack
- **Backend**: Java 17, Spring Boot, Spring Data JPA, Hibernate, MySQL.
- **Frontend**: Vue 3 (Composition API), Vite, Vue Router, Leaflet (OpenStreetMap).
- **Architecture**: RESTful API with a Service-oriented backend.

---

## ⚙️ Setup & Installation

### 1. Database Setup
1. Start your MySQL server (e.g., via XAMPP Control Panel).
2. Create a database named `ara_db`.
3. The application will automatically generate tables and seed initial data (`dispatcher1`, `driver1`, `hospitals`, `ambulances`) on the first run.

### 2. Backend Installation
1. Navigate to the root directory.
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

### 3. Frontend Installation
1. Navigate to the `frontend/` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install --legacy-peer-deps
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   *The frontend runs at `http://localhost:5173`.*

---

## 🔄 Mission Lifecycle & Flow

1.  **Incident Logging**: Dispatcher enters patient details and emergency type in the **New Request** form.
2.  **Unit Assignment**: Selecting an available ambulance automatically identifies its base hospital.
3.  **Route Optimization**: 
    *   Clicking **"Generate Optimal Path"** triggers the Bellman-Ford engine.
    *   The map visualizes the relaxation steps and highlights the final optimal green path.
4.  **Dispatch**: Clicking **"Dispatch"** creates a `RequestLog` and notifies the assigned driver.
5.  **Navigation**: The Driver dashboard instantly displays the patient's location and the calculated route.
6.  **Status Progression**: Driver updates status: `EN_ROUTE` ➔ `TRANSPORT` ➔ `COMPLETED`.
7.  **Auto-Reset**: Upon completion, the ambulance returns to `AVAILABLE` status, and both dashboards clear for the next emergency.

---

## 🧠 Core Algorithm: Bellman-Ford
The routing engine is built to be more robust than standard Dijkstra:
- **Negative Edges**: Injected to simulate high-priority corridors or specific traffic conditions.
- **Cycle Detection**: Warns dispatchers if a negative cycle (unstable traffic loop) is detected in the network.
- **Visualization Data**: Returns thousands of relaxation steps allowing the frontend to replay the algorithm's execution logic.

## 🔐 Default Credentials
| Role | Username | Password |
| :--- | :--- | :--- |
| **Dispatcher** | `dispatcher1` | `password` |
| **Driver** | `driver1` | `password` |
