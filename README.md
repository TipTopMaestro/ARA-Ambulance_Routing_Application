# ARA Ambulance Routing Application a Bellman-Ford Algorithm path finding driven application

Simple Spring Boot backend demonstrating a Bellman-Ford shortest-path API.

## Run

1. Build: `mvn clean package`
2. Run: `mvn spring-boot:run`

## API

POST `/api/path`

Request body:

```json
{
  "source": { "id": "A", "latitude": 0.0, "longitude": 0.0 },
  "target": { "id": "D", "latitude": 0.0, "longitude": 0.0 },
  "edges": [
    { "source": "A", "target": "B", "weight": 2.0 },
    { "source": "B", "target": "C", "weight": 3.0 },
    { "source": "A", "target": "C", "weight": 5.0 },
    { "source": "C", "target": "D", "weight": 1.0 }
  ]
}
```

Response contains the shortest path, distance, and negative cycle detection.

## Frontend

A sample React frontend is available in `frontend/`.

1. Copy `frontend/.env.example` to `frontend/.env`.
2. Add your Google Maps API key to `VITE_GOOGLE_MAPS_API_KEY`.
3. Run:
   - `cd frontend`
   - `npm install`
   - `npm run dev`

The frontend uses the Spring Boot backend at `http://localhost:8080/api/path` and will display the computed path on Google Maps.
