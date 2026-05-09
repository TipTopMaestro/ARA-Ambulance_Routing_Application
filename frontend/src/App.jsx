import { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

function Map({ pathCoordinates, allNodes, onNodeClick }) {
  const mapRef = useRef(null);
  const mapInstance = useRef(null);
  const polylineRef = useRef(null);
  const markersRef = useRef([]);
  const backgroundMarkersRef = useRef([]);

  useEffect(() => {
    if (!mapRef.current) {
      return;
    }

    if (!mapInstance.current) {
      mapInstance.current = L.map(mapRef.current).setView([7.3081, 125.6841], 14);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(mapInstance.current);
    }

    const map = mapInstance.current;

    // Render all significant nodes as background interactive points
    backgroundMarkersRef.current.forEach(marker => map.removeLayer(marker));
    backgroundMarkersRef.current = [];

    allNodes.forEach(node => {
      const isHospital = node.id.startsWith('H');
      const isAmbulance = node.id.startsWith('AMB');
      
      const marker = L.circleMarker([node.latitude, node.longitude], {
        radius: isHospital ? 8 : (isAmbulance ? 7 : 5),
        fillColor: isHospital ? '#ef4444' : (isAmbulance ? '#f59e0b' : '#3b82f6'),
        color: '#ffffff',
        weight: 2,
        opacity: 1,
        fillOpacity: 0.9
      }).addTo(map);

      marker.on('click', () => {
        onNodeClick(node.id, node.latitude, node.longitude, node.name);
      });

      marker.bindTooltip(node.name || node.id, { permanent: false, direction: 'top' });
      backgroundMarkersRef.current.push(marker);
    });

    console.log(`Rendered ${allNodes.length} interaction nodes.`);

    // Clear existing polyline and markers
    if (polylineRef.current) {
      map.removeLayer(polylineRef.current);
    }
    markersRef.current.forEach(marker => map.removeLayer(marker));
    markersRef.current = [];

    if (pathCoordinates && pathCoordinates.length >= 2) {
      const pathCoordsList = pathCoordinates.map((node) => [node.lat, node.lng]);
      
      polylineRef.current = L.polyline(pathCoordsList, {
        color: '#ef4444',
        opacity: 0.85,
        weight: 6,
      }).addTo(map);

      // Add visible pointer nodes
      pathCoordinates.forEach((node, index) => {
        const isStart = index === 0;
        const isEnd = index === pathCoordinates.length - 1;
        
        let fillColor = '#ffffff';
        let radius = 6;
        let color = '#ef4444';

        if (isStart) {
          fillColor = '#22c55e'; // Green for start
          radius = 8;
          color = '#166534';
        } else if (isEnd) {
          fillColor = '#ef4444'; // Red for end
          radius = 8;
          color = '#991b1b';
        }

        const marker = L.circleMarker([node.lat, node.lng], {
          radius: radius,
          fillColor: fillColor,
          color: color,
          weight: 3,
          opacity: 1,
          fillOpacity: 1
        }).addTo(map);

        markersRef.current.push(marker);
      });
      
      map.fitBounds(polylineRef.current.getBounds(), { padding: [50, 50] });
    }
  }, [pathCoordinates, allNodes]);

  return <div className="map-root" ref={mapRef} />;
}

function App() {
  const [sourceId, setSourceId] = useState('CITY_HALL');
  const [destinationId, setDestinationId] = useState('H1');
  const [status, setStatus] = useState('Ready');
  const [routeCoordinates, setRouteCoordinates] = useState(null);
  const [estimatedTime, setEstimatedTime] = useState(null);
  const [responseMessage, setResponseMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [allNodes, setAllNodes] = useState([]);

  useEffect(() => {
    const fetchNodes = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/nodes');
        if (response.ok) {
          const data = await response.json();
          setAllNodes(data);
        }
      } catch (error) {
        console.error('Failed to fetch nodes:', error);
      }
    };
    fetchNodes();
  }, []);

  const [selectionStep, setSelectionStep] = useState('source'); // 'source' or 'destination'

  const handleNodeClick = (nodeId, lat, lon, name) => {
    if (selectionStep === 'source') {
      setSourceId(nodeId);
      setSelectionStep('destination');
      setRouteCoordinates(null); // Clear previous route
      setEstimatedTime(null);
      setResponseMessage(`Source set to: ${name || nodeId}. Now select a destination.`);
    } else {
      setDestinationId(nodeId);
      setSelectionStep('source'); // Reset for next pair selection
      setResponseMessage(`Destination set to: ${name || nodeId}. Click Calculate to find route.`);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setStatus('Calculating...');
    // setResponseMessage(''); // Keep selection message
    setRouteCoordinates(null);
    setEstimatedTime(null);

    try {
      const body = { sourceId, targetId: destinationId };
      const response = await fetch('http://localhost:8080/api/path', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      const data = await response.json();

      if (!response.ok) {
        setStatus('Error');
        setResponseMessage(data.message || 'Unable to compute route.');
      } else {
        setStatus(data.negativeCycleDetected ? 'Warning: Traffic loop detected' : 'Optimal Route Found');
        setResponseMessage(data.message);
        setRouteCoordinates(data.coordinates);
        setEstimatedTime(data.estimatedTime);
      }
    } catch (error) {
      setStatus('Failed');
      setResponseMessage(error.message || 'Unknown error. Ensure backend is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <div className="header card">
        <div>
          <h1>Emergency Dispatcher Dashboard</h1>
          <p>Panabo City Ambulance Routing System</p>
        </div>
        <div className="status">
          <div style={{ textAlign: 'right' }}>
            <strong>Step:</strong> <span style={{ color: selectionStep === 'source' ? '#3b82f6' : '#ef4444', fontWeight: 'bold' }}>
              {selectionStep === 'source' ? 'Picking Source' : 'Picking Destination'}
            </span>
            <br />
            <small style={{ color: '#6b7280' }}>Click any node on the map</small>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
        <div className="card form-grid">
          <form onSubmit={handleSubmit} className="form-grid">
            
            <div>
              <h2 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>1. Select Available Ambulance / Hospital</h2>
              <select 
                value={sourceId} 
                onChange={(e) => setSourceId(e.target.value)}
                style={{ width: '100%', padding: '0.8rem', borderRadius: '0.75rem', border: '1px solid #d1d5db', fontSize: '1rem' }}
              >
                {allNodes.filter(n => n.id.startsWith('H') || n.id.startsWith('AMB') || n.id === 'CITY_HALL').map(n => (
                  <option key={n.id} value={n.id}>{n.name || n.id}</option>
                ))}
              </select>
            </div>

            <div>
              <h2 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>2. Select Emergency Destination (Patient)</h2>
              <select 
                value={destinationId} 
                onChange={(e) => setDestinationId(e.target.value)}
                style={{ width: '100%', padding: '0.8rem', borderRadius: '0.75rem', border: '1px solid #d1d5db', fontSize: '1rem' }}
              >
                {allNodes.map(n => (
                  <option key={n.id} value={n.id}>{n.name || n.id}</option>
                ))}
              </select>
            </div>

            <button type="submit" disabled={loading} style={{ marginTop: '1rem', backgroundColor: '#ef4444' }}>
              {loading ? 'Calculating...' : 'Calculate Optimal Route'}
            </button>
          </form>

          {estimatedTime !== null && estimatedTime !== Infinity && (
            <div style={{ marginTop: '1.5rem', padding: '1rem', backgroundColor: '#f0fdf4', borderRadius: '0.75rem', border: '1px solid #bbf7d0' }}>
              <h3 style={{ margin: 0, color: '#166534', fontSize: '1.1rem' }}>Estimated Travel Time:</h3>
              <p style={{ margin: '0.5rem 0 0 0', fontSize: '1.5rem', fontWeight: 'bold', color: '#15803d' }}>
                {estimatedTime.toFixed(1)} mins
              </p>
            </div>
          )}

          {responseMessage && <p className="footer" style={{ marginTop: '1rem' }}><strong>System log:</strong> {responseMessage}</p>}
        </div>

        <div className="card map-panel">
          <Map pathCoordinates={routeCoordinates} allNodes={allNodes} onNodeClick={handleNodeClick} />
        </div>
      </div>
    </div>
  );
}

export default App;