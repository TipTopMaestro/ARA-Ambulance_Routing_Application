# ARA: Ambulance Routing Application (Reworked)

ARA is a state-of-the-art emergency response management system designed to optimize ambulance dispatch and routing for Panabo City. It leverages the **Bellman-Ford Algorithm** to calculate shortest paths while accounting for dynamic factors like traffic anomalies (simulated via negative edges).

## 🚀 Key Features

### 📡 Dispatcher Dashboard
- **Incident Management:** Rapidly input patient details and emergency types.
- **Dynamic Hospital Selection:** Interactive map-based or dropdown selection of hospitals (Starting Points).
- **Unit Assignment Modal:** Real-time availability tracking of ambulances stationed at hospitals.
- **Route Optimization:** calculates ETAs and shortest routes using a robust Java implementation of the Bellman-Ford algorithm.
- **Fleet Monitoring:** A quick-look panel showing the status of all units (Available, Reserved, Busy).
- **Mission Logs:** Historical tracking of all dispatched missions with timestamps and route data.

### 🚒 Driver Dashboard
- **Instant Mission Receipt:** Real-time reception of patient information, route maps, and ETAs.
- **Navigation Map:** Dedicated map highlighting the shortest route from the hospital to the patient.
- **Status Progression:** One-touch status updates: *En Route* ➔ *Transport* ➔ *Completed*.
- **Auto-Reset:** Upon completion, the dashboard resets for the next incident, and the ambulance is automatically marked as *Available* for dispatchers.

## 🛠 Tech Stack
- **Backend:** Java 17, Spring Boot, Spring Data JPA, Hibernate.
- **Frontend:** React (Vite), React Router, Leaflet (OpenStreetMap), React Context API.
- **Database:** MySQL (External persistence).
- **Algorithm:** Bellman-Ford (supports negative weights and cycle detection).

## 📋 Prerequisites
- **Java:** JDK 17 or higher.
- **Node.js:** v18 or higher.
- **Database:** MySQL (e.g., XAMPP, WAMP, or standalone MySQL Server).
- **Build Tool:** Maven.

## ⚙️ Setup & Installation

### 1. Database Setup
1. Start your MySQL server (e.g., via XAMPP Control Panel).
2. The application will automatically create the `ara_db` database on first run (as configured in `application.properties`).
   - Default connection: `localhost:3306`, User: `root`, Password: (none).

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
   *Note: On first start, the system seeds initial users (`dispatcher1`, `driver1`, `driver2`) and ambulances.*

### 3. Frontend Installation
1. Navigate to the `frontend/` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies (requires `--legacy-peer-deps` due to Vite/Vite-Plugin-React versioning):
   ```bash
   npm install --legacy-peer-deps
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

## 🔐 Credentials (Default)
| Role | Username | Password |
| :--- | :--- | :--- |
| **Dispatcher** | `
` | `password` |
| **Driver** | `driver1` | `password` |

## 🧠 Algorithm: Bellman-Ford
The routing engine uses the Bellman-Ford algorithm to ensure reliability in complex graph scenarios. 
- **Negative Edges:** The system randomly injects negative weights into the map edges to simulate special conditions (e.g., shortcuts or high-priority traffic corridors).
- **Cycle Detection:** If a negative cycle is detected, the system warns the dispatcher that traffic loops may lead to unstable routing.

## 🗺 Data Source
Map data is derived from **OpenStreetMap (OSM)** via the Overpass API, covering significant nodes, intersections, and hospital locations in Panabo City.
