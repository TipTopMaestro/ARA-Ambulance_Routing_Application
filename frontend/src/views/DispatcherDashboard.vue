<template>
  <div class="dispatcher-dashboard" :class="{ 'left-collapsed': !isLeftPanelOpen, 'right-collapsed': !isRightPanelOpen, 'is-mobile': isMobile, 'no-transition': isInitialLoad }"> 
    
    <!-- Backdrop for mobile drawers -->
    <div v-if="isMobile && (isLeftPanelOpen || isRightPanelOpen)" class="drawer-backdrop" @click="closeDrawers"></div>

    <!-- Left Panel: Emergency Details Form -->
    <div class="left-panel" :class="{ 'panel-open': isLeftPanelOpen }">
      <div class="card-header">
        <div class="panel-header">
          <h2 class="panel-title">Emergency Details</h2>
        </div>
        <div class="reset-container">
          <button @click="resetForm" class="reset-btn">
            <i class="bi bi-arrow-counterclockwise"></i>
            Reset
          </button>
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">Patient Name:</label>
        <input type="text" v-model="patientName" placeholder="Enter patient name" class="form-input" />
      </div>
      
      <div class="form-group">
        <label class="form-label">Emergency Type:</label>
        <input type="text" v-model="emergencyType" placeholder="e.g. Cardiac Arrest, Trauma" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">Source Hospital:</label>
        <select v-model="selectedHospital" class="form-select">
          <option value="">Select Hospital</option>
          <option v-for="n in allNodes.filter(node => node.id.startsWith('H'))" :key="n.id" :value="n.id">
            {{ n.name || n.id }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">Patient Location:</label>

        <div class="form-select-wrapper">
          <input
            v-model="destinationId"
            list="locationSuggestions"
            class="form-select select-like-input"
            placeholder="Select, click map, or type location"
          />
          <span class="select-chevron" aria-hidden="true"></span>
        </div>

        <datalist id="locationSuggestions">
          <option
            v-for="n in allNodes"
            :key="n.id"
            :value="n.id"
          >
            {{ n.name || n.id }}
          </option>
        </datalist>
      </div>

      <div class="form-group form-group-large">
        <label class="form-label">Ambulance / Unit:</label>
        <select v-model="selectedAmbulance" class="form-select">
          <option value="">Select Ambulance</option>
          <option v-for="amb in ambulances" :key="amb.id" :value="amb.id">
            {{ amb.id }} - {{ amb.status }}
          </option>
        </select>
      </div>

      <button 
        @click="handleCalculateRoute" 
        :disabled="!selectedHospital || !destinationId || loading"
        class="btn-primary btn-large"
      >
        GENERATE ROUTE
      </button>

      <!-- ETA Section -->
      <div class="eta-section">
        <h3 class="eta-title">ETA</h3>

        <div class="eta-item">
          <div class="eta-value" :class="{ 'warning-text': routeData?.negativeCycleDetected }">
            {{ formatETA(routeData?.estimatedTime) }} min
          </div>
        </div>

        <div v-if="routeData?.negativeCycleDetected" class="cycle-warning">
          <i class="bi bi-exclamation-triangle-fill"></i>
          <span>Negative cycle detected! Distances may be unstable.</span>
        </div>

        <div class="eta-item">
          <label class="eta-label">Starting From:</label>
          <div class="eta-hospital">
            <div v-if="selectedHospital">
              <div style="font-weight:600">{{ allNodes.find(n => n.id === selectedHospital)?.name || selectedHospital }}</div>
            </div>
            <div v-else>
              Not selected
            </div>
          </div>
        </div>

        <div class="eta-item">
          <label class="eta-label">Patient Location:</label>
          <div class="eta-location">
            <div v-if="selectedNode">
              <div style="font-weight:600">{{ selectedNode.name || selectedNode.id }}</div>
              <div style="font-size:0.85rem; color:#6b7280">{{ selectedNode.address || (selectedNode.latitude && selectedNode.longitude ? `Coords: ${selectedNode.latitude}, ${selectedNode.longitude}` : '') }}
              </div>
            </div>
            <div v-else>
              Not selected
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="map-panel">
      <Map 
        :allNodes="allNodes" 
        :pathCoordinates="routeData?.coordinates" 
        :onNodeClick="handleNodeClick"
        :selectedSource="selectedHospital"
        :selectedDestination="destinationId"
        :allEdges="allEdges"
        :staticEdges="staticEdges"
        :showGraphEdges="showGraphEdges"
        :showNodeIds="showNodeIds"
        :showMapNodes="showMapNodes"
        :viewCenter="mapCenter"
        :viewZoom="mapZoom"
        @view-changed="handleMapViewChanged"
        :currentTraversedNode="currentTraversedNode"
        :currentRelaxedEdge="currentRelaxedEdge"
        :visitedNodes="visitedNodes"
      />
    </div>

    <!-- Panel Toggles (Moved to dashboard root) -->
    <button 
      @click="toggleLeftPanel" 
      class="panel-toggle left-toggle" 
      :class="{ 'is-open': isLeftPanelOpen }"
      :title="isLeftPanelOpen ? 'Collapse Details' : 'Expand Details'"
    >
      <i class="bi" :class="isLeftPanelOpen ? 'bi-chevron-left' : 'bi-chevron-right'"></i>
    </button>

    <button 
      @click="toggleRightPanel" 
      class="panel-toggle right-toggle" 
      :class="{ 'is-open': isRightPanelOpen }"
      :title="isRightPanelOpen ? 'Hide Status' : 'Show Status'"
    >
      <i class="bi" :class="isRightPanelOpen ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
    </button>

    <!-- Right Panel: Fleet Status & System Status -->
    <div class="right-panel" :class="{ 'panel-open': isRightPanelOpen }">
      <div class="action-panel">
        <label class="form-label">Current Action: 
          <span class="current-action-value">{{ currentAction }}...</span>
        </label>
        <div class="system-log-message" :class="{ 'warning-text': routeData?.negativeCycleDetected }">
          {{ systemLog }}
        </div>
      </div>
      <!-- Fleet Status Card -->
      <div class="fleet-status">
        <h3 class="panel-subtitle">Fleet Status</h3>
        
        <div class="table-wrapper">
          <table class="fleet-table">
            <thead>
              <tr class="table-header">
                <th class="table-header-cell">Unit</th>
                <th class="table-header-cell">Status</th>
                <th class="table-header-cell">Hospital</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="amb in ambulances" :key="amb.id" class="table-row">
                <td class="table-cell">{{ amb.id }}</td>
                <td class="table-cell">
                  <div class="status-indicator">
                    <span 
                      class="status-dot"
                      :class="amb.status === 'AVAILABLE' ? 'status-available' : 'status-busy'"
                    ></span>
                    <span class="status-text">{{ amb.status }}</span>
                  </div>
                </td>
                <td class="table-cell">{{ amb.hospitalId }}</td>
              </tr>
              <tr v-if="ambulances.length === 0" class="table-row">
                <td colspan="3" class="table-empty">No ambulances available</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Map Layers Card -->
      <div class="system-status" style="margin-bottom: 0;">
        <h3 class="panel-subtitle">Map Layers</h3>
        <div class="status-items">
          <div class="status-item">
            <span class="status-label">Show Map Nodes:</span>
            <label class="switch">
              <input type="checkbox" v-model="showMapNodes">
              <span class="slider round"></span>
            </label>
          </div>
          <div class="status-item">
            <span class="status-label">Show Graph Network:</span>
            <label class="switch">
              <input type="checkbox" v-model="showGraphEdges">
              <span class="slider round"></span>
            </label>
          </div>
          <div class="status-item">
            <span class="status-label">Show Node IDs:</span>
            <label class="switch">
              <input type="checkbox" v-model="showNodeIds">
              <span class="slider round"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- System Status Card -->
      <div class="system-status">
        <h3 class="panel-subtitle">System status</h3>
        
        <div class="status-items">
          <div class="status-item">
            <span class="status-label">Ambulances Active:</span>
            <strong class="status-number">{{ ambulances.filter(a => a.status !== 'AVAILABLE').length }}</strong>
          </div>
          
          <div class="status-item">
            <span class="status-label">Ambulances Available:</span>
            <strong class="status-number status-available-count">{{ ambulances.filter(a => a.status === 'AVAILABLE').length }}</strong>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import Map from '../components/Map.vue'
import { useAuth } from '../composables/useAuth'

const { user } = useAuth()
const allNodes = ref([])
const allEdges = ref([])
const staticEdges = ref([])
const showGraphEdges = ref(true)
const showNodeIds = ref(false)
const showMapNodes = ref(true)
const mapCenter = ref([7.3081, 125.6841])
const mapZoom = ref(14)

// Layout state for responsiveness and collapsible panels
const isMobile = ref(false)
const isLeftPanelOpen = ref(true)
const isRightPanelOpen = ref(true)
const isInitialLoad = ref(true)

const loadPanelState = () => {
  const leftSaved = localStorage.getItem('ara_left_panel_open')
  const rightSaved = localStorage.getItem('ara_right_panel_open')
  const graphSaved = localStorage.getItem('ara_show_graph_edges')
  const nodeIdsSaved = localStorage.getItem('ara_show_node_ids')
  const mapNodesSaved = localStorage.getItem('ara_show_map_nodes')
  const mapCenterSaved = localStorage.getItem('ara_map_center')
  const mapZoomSaved = localStorage.getItem('ara_map_zoom')
  
  if (isMobile.value) {
    isLeftPanelOpen.value = false
    isRightPanelOpen.value = false
  } else {
    isLeftPanelOpen.value = leftSaved === null ? true : leftSaved === 'true'
    isRightPanelOpen.value = rightSaved === null ? true : rightSaved === 'true'
  }

  showGraphEdges.value = graphSaved === null ? true : graphSaved === 'true'
  showNodeIds.value = nodeIdsSaved === null ? false : nodeIdsSaved === 'true'
  showMapNodes.value = mapNodesSaved === null ? true : mapNodesSaved === 'true'
  
  if (mapCenterSaved) mapCenter.value = JSON.parse(mapCenterSaved)
  if (mapZoomSaved) mapZoom.value = parseInt(mapZoomSaved)
}

const savePanelState = () => {
  if (!isMobile.value) {
    localStorage.setItem('ara_left_panel_open', isLeftPanelOpen.value)
    localStorage.setItem('ara_right_panel_open', isRightPanelOpen.value)
  }
}

const saveMapLayerState = () => {
  localStorage.setItem('ara_show_graph_edges', showGraphEdges.value)
  localStorage.setItem('ara_show_node_ids', showNodeIds.value)
  localStorage.setItem('ara_show_map_nodes', showMapNodes.value)
}

const handleMapViewChanged = (view) => {
  mapCenter.value = view.center
  mapZoom.value = view.zoom
  localStorage.setItem('ara_map_center', JSON.stringify(view.center))
  localStorage.setItem('ara_map_zoom', view.zoom)
}

watch(showGraphEdges, saveMapLayerState)
watch(showNodeIds, saveMapLayerState)
watch(showMapNodes, saveMapLayerState)

const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 1024
  loadPanelState()
}

// Initialize immediately in setup to prevent animation on reload
if (typeof window !== 'undefined') {
  checkScreenSize()
}

const toggleLeftPanel = () => {
  isLeftPanelOpen.value = !isLeftPanelOpen.value
  if (isMobile.value && isLeftPanelOpen.value) {
    isRightPanelOpen.value = false // Close other panel on mobile
  }
  savePanelState()
}

const toggleRightPanel = () => {
  isRightPanelOpen.value = !isRightPanelOpen.value
  if (isMobile.value && isRightPanelOpen.value) {
    isLeftPanelOpen.value = false // Close other panel on mobile
  }
  savePanelState()
}

const closeDrawers = () => {
  isLeftPanelOpen.value = false
  isRightPanelOpen.value = false
}

// Mockup units (used until backend provides real data)
const ambulances = ref([
  { id: 'AMB01', status: 'AVAILABLE', hospitalId: 'RMCI', hospitalName: 'RMCI Medical Center', hospitalOsmNodeId: 'H1' },
  { id: 'AMB02', status: 'BUSY', hospitalId: 'CGH', hospitalName: 'City General Hospital', hospitalOsmNodeId: 'H2' }
])

const patientName = ref('')
const emergencyType = ref('')
const selectedHospital = ref('') // Internal source, usually inferred from ambulance
const selectedAmbulance = ref('')
const destinationId = ref('')

// computed selected node details for ETA location display
const selectedNode = computed(() => {
  if (!destinationId.value) return null
  return allNodes.value.find(n => n.id === destinationId.value) || null
})

// when dispatcher selects an ambulance, auto-fill assigned hospital info
watch(selectedAmbulance, (val) => {
  if (!val) return
  // Logic could go here if needed to auto-select hospital
})

const routeData = ref(null)
const loading = ref(false)
const systemLog = ref('Ready')

// human-friendly current action status for the UI (shows what the user is about to do)
  const currentAction = computed(() => {
    if (loading.value) return 'Calculating route'

    // Primary UI flow states (only these three messages are used):
    // 1) Adding patient location — when destination not chosen
    if (!destinationId.value) return 'Adding patient location'

    // 2) Selecting unit — when destination chosen but no ambulance selected
    if (!selectedAmbulance.value) return 'Selecting unit'

    // 3) Calculating route — when both destination and ambulance are selected
    return 'Calculating route'
  })

const currentTraversedNode = ref(null)
const currentRelaxedEdge = ref(null)
const visitedNodes = ref([])

let pollInterval = null

onMounted(async () => {
  // Ensure the no-transition class is removed after the first render
  setTimeout(() => {
    isInitialLoad.value = false
  }, 100)
  
  window.addEventListener('resize', checkScreenSize)
  await fetchNodes() // Wait for nodes to load first
  fetchEdges()
  fetchAmbulances()
  pollInterval = setInterval(() => {
    fetchAmbulances()
  }, 5000)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
  if (pollInterval) clearInterval(pollInterval)
})

const fetchNodes = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/nodes')
    if (res.ok) allNodes.value = await res.json()
  } catch (e) { console.error("Failed to fetch nodes", e) }
}

const fetchEdges = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/edges')
    if (res.ok) {
      staticEdges.value = await res.json()
    }
  } catch (e) { console.error("Failed to fetch edges", e) }
}

const fetchAmbulances = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/ambulances')
    if (res.ok) {
      const data = await res.json()
      // Only override the mockups if backend returns real list
      if (Array.isArray(data) && data.length > 0) ambulances.value = data
    }
  } catch (e) { console.error("Failed to fetch ambulances", e) }
}

const formatETA = (time) => {
  if (time === null || time === undefined) return '0.0'
  if (time === Infinity || time === 'Infinity') return '∞'
  return parseFloat(time).toFixed(1)
}

const handleNodeClick = (id) => {
  destinationId.value = id
  systemLog.value = `Destination set to: ${id}`
}

const handleCalculateRoute = async () => {
  if (!selectedHospital.value || !destinationId.value) {
    systemLog.value = 'Please select a source hospital and destination location.'
    return
  }

  const sourceId = selectedHospital.value

  loading.value = true
  routeData.value = null
  systemLog.value = 'Initiating Bellman-Ford shortest path analysis...'
  try {
    const res = await fetch('http://localhost:8081/api/path', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sourceId, targetId: destinationId.value }),
    })
    const data = await res.json()
    allEdges.value = data.steps
    
    if (data.relaxationSteps && data.relaxationSteps.length > 0) {
      await visualizeAlgorithm(data.relaxationSteps)
    }

    // --- NEW: OSM Road Snapping (CURVY & FLEXIBLE) ---
    if (data.coordinates && data.coordinates.length >= 2) {
      try {
        systemLog.value = 'Generating high-resolution road path...'
        const coordString = data.coordinates
          .map(c => `${c.lng || c.longitude},${c.lat || c.latitude}`)
          .join(';')
        
        // Use 'route' with overview=full to get all the curves and turns of the streets
        const osrmRes = await fetch(`https://router.project-osrm.org/route/v1/driving/${coordString}?overview=full&geometries=geojson&continue_straight=false`)
        const osrmData = await osrmRes.json()
        
        if (osrmData.code === 'Ok' && osrmData.routes && osrmData.routes.length > 0) {
          // Extract the high-resolution geometry coordinates
          const snappedCoords = osrmData.routes[0].geometry.coordinates.map(c => ({
            lat: c[1],
            lng: c[0]
          }))
          // Replace the sparse node coordinates with dense road coordinates
          data.coordinates = snappedCoords
        }
      } catch (osrmErr) {
        console.error('OSM Road Generation failed, falling back to straight edges:', osrmErr)
      }
    }
    // --------------------------------------------------

    routeData.value = data
    systemLog.value = data.message
  } catch (err) {
    systemLog.value = 'Route calculation failed'
  } finally {
    loading.value = false
  }
}

const visualizeAlgorithm = async (steps) => {
  visitedNodes.value = []
  const visitedSet = new Set()

  // Cap steps to prevent UI freeze during negative cycle detection
  const visualizationSteps = steps.slice(0, 300) 
  
  for (let i = 0; i < visualizationSteps.length; i++) {
    const step = visualizationSteps[i]
    currentTraversedNode.value = step.targetId
    currentRelaxedEdge.value = { sourceId: step.sourceId, targetId: step.targetId }
    
    if (!visitedSet.has(step.targetId)) {
      visitedSet.add(step.targetId)
      // Only update the reactive array every 10 steps to reduce map re-renders
      if (i % 10 === 0 || i === visualizationSteps.length - 1) {
        visitedNodes.value = Array.from(visitedSet)
      }
    }

    // Small delay to allow UI to breathe
    await new Promise(resolve => setTimeout(resolve, 5))
  }
  
  currentTraversedNode.value = null
  currentRelaxedEdge.value = null
}

const handleDispatch = async () => {
  if (!selectedAmbulance.value || !routeData.value) return

  const mission = {
    patientName: patientName.value,
    emergencyType: emergencyType.value,
    ambulance: { id: selectedAmbulance.value },
    dispatcher: { id: user.value?.id || 1 },
    status: 'DISPATCHED',
    pathJson: JSON.stringify(routeData.value.coordinates)
  }

  const res = await fetch('http://localhost:8081/api/missions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(mission),
  })

  if (res.ok) {
    systemLog.value = 'Mission dispatched successfully!'
    resetForm()
    fetchAmbulances()
  } else {
    systemLog.value = 'Mission dispatch failed'
  }
}

const resetForm = () => {
  patientName.value = ''
  emergencyType.value = ''
  selectedHospital.value = ''
  selectedHospitalId.value = null
  selectedAmbulance.value = ''
  destinationId.value = ''
  locationDescription.value = ''
  routeData.value = null
  visitedNodes.value = []
  systemLog.value = 'Form reset'
}
</script>

<style scoped>
.dispatcher-dashboard.no-transition,
.dispatcher-dashboard.no-transition * {
  transition: none !important;
}

/* Main Container */
.dispatcher-dashboard {
  padding: 1rem;
  display: grid;
  grid-template-columns: 350px 1fr 300px;
  gap: 1rem;
  height: calc(100vh - 80px);
  background-color: #f3f4f6;
  transition: grid-template-columns 0.3s ease;
  position: relative;
}

.dispatcher-dashboard.left-collapsed {
  grid-template-columns: 0px 1fr 300px;
}

.dispatcher-dashboard.right-collapsed {
  grid-template-columns: 350px 1fr 0px;
}

.dispatcher-dashboard.left-collapsed.right-collapsed {
  grid-template-columns: 0px 1fr 0px;
}

/* Card Base Style */
.card,
.left-panel,
.map-panel,
.fleet-status,
.system-status {
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Left Panel */
.left-panel {
  overflow-y: auto;
  padding: 1.5rem;
  transition: transform 0.3s ease, opacity 0.3s ease;
  z-index: 10;
}

.left-collapsed .left-panel {
  transform: translateX(-100%);
  opacity: 0;
  pointer-events: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.panel-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: bold;
  color: #374151;
}

.reset-container {
  display: flex;
  padding: 0.1rem;
  align-items: center;
  margin-bottom: 1.5rem;
  background-color: #374151;
  border-radius: 10px;
}

.reset-btn {
  padding: 0.4rem;
  background-color: transparent;
  font-size: 0.75rem;
  color: #ffffff; 
  cursor: pointer;
  font-weight: 600;
}

/* Form Elements */
.form-group {
  margin-bottom: 0.7rem;
}

.form-group-large {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-size: 0.8rem;
  font-weight: 600;
  color: #374151;
}

.form-input,
.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.85rem;
  font-family: inherit;
  box-sizing: border-box;
  background-color: white;
  transition: all 0.2s ease;
}

.form-input:hover,
.form-select:hover {
  border-color: #9ca3af;
  background-color: #fafbfc;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  background-color: white;
}

select.form-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23374151' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  padding-right: 2.5rem;
}

/* Buttons */
.btn-primary {
  width: 100%;
  padding: 0.6rem;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-large {
  font-size: 0.8rem;
  margin-bottom: 1rem;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ETA Section */
.eta-section {
  background-color: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  padding: 0.8rem;
}

.eta-title {
  font-weight: bold;
  color: #374151;
  margin: 0 0 0.5rem 0;
  font-size: 0.95rem;
}

.eta-item {
  margin-bottom: 0.5rem;
}

.eta-label {
  font-size: 0.8rem;
  color: #6b7280;
  display: block;
  margin-bottom: 0.3rem;
}

.eta-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #374151;
}

.eta-location {
  font-size: 0.8rem;
  color: #374151;
  background-color: #f9fafb;
  padding: 0.5rem;
  border-radius: 4px;
}

.eta-hospital {
  font-size: 0.8rem;
  color: #374151;
  background-color: #f9fafb;
  padding: 0.5rem;
  border-radius: 4px;
}

/* Center Panel: Map */
.map-panel {
  padding: 0;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

/* Panel Toggles */
.panel-toggle {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  background-color: #ffffff;
  border: 1px solid #d1d5db;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2000; /* Higher than panels */
  transition: all 0.3s ease;
  padding: 0;
  border-radius: 0;
}

.panel-toggle:hover {
  background-color: #f9fafb;
  color: #111827;
}

.left-toggle {
  left: 350px; /* Positioned at the edge of the left panel */
  border-left: none;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  box-shadow: 2px 0 5px rgba(0,0,0,0.05);
}

.left-collapsed .left-toggle {
  left: 0;
}

.right-toggle {
  right: 300px; /* Positioned at the edge of the right panel */
  border-right: none;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  box-shadow: -2px 0 5px rgba(0,0,0,0.05);
}

.right-collapsed .right-toggle {
  right: 0;
}

/* Right Panel */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow: hidden;
  transition: transform 0.3s ease, opacity 0.3s ease;
  z-index: 10;
}

.right-collapsed .right-panel {
  transform: translateX(100%);
  opacity: 0;
  pointer-events: none;
}

.action-panel {
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.system-log-message {
  font-size: 0.85rem;
  color: #6b7280;
  font-style: italic;
}

.warning-text {
  color: #dc2626 !important;
}

.cycle-warning {
  background-color: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 6px;
  padding: 0.75rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #dc2626;
  font-size: 0.85rem;
  font-weight: 500;
  line-height: 1.25;
}

.cycle-warning i {
  font-size: 1.1rem;
}

.current-action-value {
  font-weight: 400;
}

/* Fleet Status */
.fleet-status {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.panel-subtitle {
  font-size: 1.1rem;
  font-weight: bold;
  color: #374151;
  margin: 0 0 1rem 0;
}

.table-wrapper {
  overflow-x: auto;
}

.fleet-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

.table-header {
  border-bottom: 1px solid #d1d5db;
}

.table-header-cell {
  text-align: left;
  padding: 0.5rem;
  font-weight: 600;
  color: #4b5563;
}

.table-row {
  border-bottom: 1px solid #f3f4f6;
}

.table-cell {
  padding: 0.5rem;
  color: #374151;
}

.table-empty {
  padding: 1rem;
  text-align: center;
  color: #9ca3af;
  font-size: 0.8rem;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-dot {
  display: inline-block;
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
}

.status-available {
  background-color: #10b981;
}

.status-busy {
  background-color: #9ca3af;
}

.status-text {
  color: #6b7280;
  font-size: 0.8rem;
}

/* System Status */
.system-status {
  flex-shrink: 0;
  padding: 1rem;
}

.status-items {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.status-label {
  color: #6b7280;
}

.status-number {
  color: #374151;
  font-size: 1rem;
}

.status-available-count {
  color: #10b981;
}

/* Toggle Switch CSS */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
}

.switch input { 
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  -webkit-transition: .4s;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  -webkit-transition: .4s;
  transition: .4s;
}

input:checked + .slider {
  background-color: #22c55e;
}

input:focus + .slider {
  box-shadow: 0 0 1px #22c55e;
}

input:checked + .slider:before {
  -webkit-transform: translateX(20px);
  -ms-transform: translateX(20px);
  transform: translateX(20px);
}

.slider.round {
  border-radius: 20px;
}

.slider.round:before {
  border-radius: 50%;
}

/* Mobile Responsiveness */
.drawer-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  z-index: 999;
}

@media (max-width: 1024px) {
  .dispatcher-dashboard {
    display: block; /* Disable grid */
    padding: 0;
    height: calc(100vh - 60px); /* Adjust for navbar */
    overflow: hidden;
  }

  .map-panel {
    height: 100%;
    width: 100%;
  }

  .left-panel, .right-panel {
    position: fixed;
    top: 60px; /* Below navbar */
    bottom: 0;
    width: 320px;
    max-width: 85%;
    margin: 0;
    border-radius: 0;
    box-shadow: 5px 0 15px rgba(0,0,0,0.1);
    z-index: 1000;
  }

  .left-panel {
    left: 0;
    transform: translateX(-100%);
    opacity: 1; /* Keep opacity for drawer feel */
  }

  .right-panel {
    right: 0;
    transform: translateX(100%);
    opacity: 1;
    box-shadow: -5px 0 15px rgba(0,0,0,0.1);
  }

  .left-panel.panel-open,
  .right-panel.panel-open {
    transform: translateX(0);
  }

  /* Adjust toggles for mobile */
  .panel-toggle {
    width: 32px;
    height: 64px;
    background-color: #ef4444; /* Make them more visible on mobile */
    color: white;
    border: none;
  }

  .left-toggle {
    top: 20%;
  }

  .right-toggle {
    top: 50%;
  }
}

@media (max-width: 640px) {
  .left-panel, .right-panel {
    width: 100%;
    max-width: 100%;
  }
}
</style>
