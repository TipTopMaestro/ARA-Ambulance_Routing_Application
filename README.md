# ARA: Ambulance Routing Application

ARA (Ambulance Routing Application) is a high-performance emergency response management system designed to optimize ambulance dispatch and routing for Panabo City. It leverages a custom **Bellman-Ford Algorithm** implementation to calculate optimal paths while accounting for dynamic factors like traffic conditions (simulated via negative edge weights).

---

## 🚀 Key Features

### 📡 Unified Dashboards & UI
- **Modern Login Experience**: A refined, high-performance login page with secure authentication, loading states, and smooth animations.
- **Role-Based Access**: Specialized views for **Dispatchers** (Incident Entry, Route Generation) and **Drivers** (Real-time Navigation, Mission Status).
- **Interactive Map**: High-performance Leaflet integration with custom layers for optimal paths and traffic anomalies.

### 🚒 Intelligent Routing & Visualization
- **Bellman-Ford Engine**: Handles complex graphs with negative weights (shortcuts/priority corridors) and detects traffic cycles.
- **Algorithm Playback**: Real-time visualization of the algorithm's relaxation process, showing how the optimal path is calculated.
- **Dynamic Re-routing**: Ability to account for changing traffic conditions and emergency priorities.

### 📊 Mission Management & Audit
- **Standardized Flow**: End-to-end mission lifecycle management from incident logging to ambulance auto-reset.
- **Audit Trails**: Every mission and generated path is persisted for post-incident review and performance analysis.
- **Fleet Tracking**: Real-time status monitoring of the entire ambulance fleet.

---

## 📂 Project Structure

```text
ARA-Ambulance_Routing_Application/
├── src/main/java/.../bellmanford/  # Spring Boot Backend
│   ├── controller/                 # REST API Endpoints
│   ├── model/                      # JPA Entities & Domain Models
│   ├── repository/                 # Data Access Layer
│   └── service/                    # Business Logic & Algorithm Engine
├── frontend/                       # Vue.js 3 Frontend
│   ├── src/views/                  # Page Components (Dashboard, Login, etc.)
│   ├── src/components/             # Reusable UI Components
│   └── src/composables/            # Shared Logic (Auth, State)
└── ara_db.sql                      # Database Schema & Seed Data
```

---

## 🛠 Tech Stack

### Backend
- **Java 25** & **Spring Boot 4.0.6**
- **Spring Data JPA** & **Hibernate**
- **MySQL** (Database)
- **JTS (Java Topology Suite)** (Spatial Data)

### Frontend
- **Vue 3** (Composition API)
- **Vite** (Build Tool)
- **Vue Router** (Navigation)
- **Leaflet** (Map Engine)
- **Modern CSS** (Responsive & Interactive Design)

---

## ⚙️ Setup & Installation

### 1. Database Setup
1. Ensure **MySQL** is running (e.g., via XAMPP or Docker).
2. Create a database named `ara_db`.
3. The application will automatically generate tables and seed initial data on the first run.

### 2. Backend Installation
1. Navigate to the root directory.
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
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
   *The frontend will be available at `http://localhost:5173`.*

---

## 🔄 Mission Lifecycle

1. **Incident Entry**: Dispatcher logs patient details and emergency status.
2. **Path Generation**: The Bellman-Ford algorithm calculates the fastest route.
3. **Dispatch**: Mission is assigned to a driver; ambulance status updates to `DISPATCHED`.
4. **Navigation**: Driver follows the optimal path on their dedicated dashboard.
5. **Completion**: Once the mission is resolved, the fleet returns to `AVAILABLE` status automatically.

---

## 🔐 Default Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Dispatcher** | `dispatcher1` | `password` |
| **Driver** | `driver1` | `password` |
