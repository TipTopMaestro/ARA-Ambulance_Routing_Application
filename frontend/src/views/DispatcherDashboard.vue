<template>
  <div class="dispatcher-dashboard" :class="{ 'left-collapsed': !isLeftPanelOpen, 'right-collapsed': !isRightPanelOpen, 'is-mobile': isMobile, 'no-transition': isInitialLoad }"> 
    
    <!-- Backdrop for mobile drawers -->
    <div v-if="isMobile && (isLeftPanelOpen || isRightPanelOpen)" class="drawer-backdrop" @click="closeDrawers"></div>

    <!-- Left Panel: Emergency Details Form -->
    <div class="left-panel" :class="{ 'panel-open': isLeftPanelOpen, 'is-deployed': isDeployed }">
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
        <input type="text" v-model="patientName" :disabled="isDeployed" placeholder="Enter patient name" class="form-input" />
      </div>
      
      <div class="form-group">
        <label class="form-label">Emergency Type:</label>
        <input type="text" v-model="emergencyType" :disabled="isDeployed" placeholder="e.g. Cardiac Arrest, Trauma" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">Source Hospital:</label>
        <select v-model="selectedHospital" :disabled="isDeployed" class="form-select">
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
            :disabled="isDeployed"
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
        <select 
          v-model="selectedAmbulance" 
          :disabled="isDeployed" 
          class="form-select status-select" 
          :class="getSelectedAmbulanceStatusClass"
        >
          <option value="">Select Ambulance</option>
          <option 
            v-for="amb in ambulances" 
            :key="amb.id" 
            :value="amb.id"
            :disabled="amb.status !== 'AVAILABLE'"
            :class="{
              'option-available': amb.status === 'AVAILABLE',
              'option-reserved': amb.status === 'RESERVED',
              'option-enroute': amb.status === 'EN_ROUTE' || amb.status === 'TRANSPORT',
              'option-busy': amb.status === 'MAINTENANCE'
            }"
          >
            {{ amb.id }} - {{ amb.status }} {{ amb.status !== 'AVAILABLE' ? '(In Use)' : '' }}
          </option>
        </select>
      </div>

      <button 
        @click="handleCalculateRoute" 
        :disabled="!patientName || !emergencyType || !selectedHospital || !destinationId || !selectedAmbulance || loading || isDeployed"
        class="btn-primary btn-large"
      >
        GENERATE ROUTE
      </button>

      <button 
        v-if="routeData"
        @click="handleDispatch" 
        :disabled="!selectedAmbulance || loading || isDeployed"
        class="btn-primary btn-large"
        style="background-color: #10b981; margin-top: -0.5rem; margin-bottom: 1rem;"
      >
        DISPATCH MISSION
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

      <!-- Tab Navigation -->
      <div class="panel-tabs">
        <button class="tab-btn" :class="{ active: rightPanelView === 'status' }" @click="rightPanelView = 'status'">
          <i class="bi bi-info-circle"></i> Status
        </button>
        <button class="tab-btn" :class="{ active: rightPanelView === 'algorithm' }" @click="rightPanelView = 'algorithm'">
          <i class="bi bi-diagram-3"></i> Algorithm
        </button>
      </div>

      <!-- VIEW 1: Fleet & System Status -->
      <div v-if="rightPanelView === 'status'" class="tab-content">
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
                        :class="{
                          'status-available': amb.status === 'AVAILABLE',
                          'status-reserved': amb.status === 'RESERVED',
                          'status-enroute': amb.status === 'EN_ROUTE' || amb.status === 'TRANSPORT',
                          'status-busy': amb.status === 'MAINTENANCE'
                        }"
                      ></span>
                      <span class="status-text">{{ amb.status }}</span>
                    </div>
                  </td>
                  <td class="table-cell">{{ amb.hospitalName }}</td>
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

      <!-- VIEW 2: Algorithm Visualization -->
      <div v-if="rightPanelView === 'algorithm'" class="tab-content algorithm-tab">
        <!-- Final Path Summary (Moved to top and always visible) -->
        <div v-if="routeData" class="final-path-summary">
          <label class="summary-label">Final Shortest Path (with cumulative cost):</label>
          <div class="path-sequence">
            <template v-for="(nodeId, idx) in routeData.path" :key="idx">
              <div class="path-node-wrapper">
                <span class="path-node">{{ nodeId }}</span>
                <span class="path-node-cost">({{ routeData.pathDistances?.[idx]?.toFixed(1) || '0.0' }})</span>
              </div>
              <i v-if="idx < routeData.path.length - 1" class="bi bi-arrow-right path-arrow"></i>
            </template>
          </div>
        </div>

        <div class="algorithm-controls">
          <div class="playback-controls">
            <button @click="togglePlayback" class="control-btn" :title="playbackState === 'playing' ? 'Pause' : 'Play'">
              <i class="bi" :class="playbackState === 'playing' ? 'bi-pause-fill' : 'bi-play-fill'"></i>
            </button>
            <button @click="stopPlayback" class="control-btn" title="Stop">
              <i class="bi bi-stop-fill"></i>
            </button>
            <button @click="restartPlayback" class="control-btn" title="Restart">
              <i class="bi bi-arrow-counterclockwise"></i>
            </button>
            <select v-model="playbackSpeed" class="speed-select-mini">
              <option :value="1000">1s</option>
              <option :value="500">0.5s</option>
              <option :value="200">0.2s</option>
              <option :value="50">0.05s</option>
            </select>
          </div>
          <div class="step-progress-mini" v-if="relaxationStepsList.length > 0">
            {{ currentStepIndex + 1 }} / {{ relaxationStepsList.length }}
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="progress-bar-container" v-if="relaxationStepsList.length > 0">
          <div class="progress-bar-fill" :style="{ width: `${((currentStepIndex + 1) / relaxationStepsList.length) * 100}%` }"></div>
        </div>
        
        <div class="table-container-right" ref="tableContainer">
          <table class="relaxation-table table-mini">
            <thead>
              <tr>
                <th style="width: 40px;">#</th>
                <th>S → T</th>
                <th style="width: 60px;">Dist</th>
                <th style="width: 40px; text-align: center;">R</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="(step, index) in relaxationStepsList" :key="index">
                <!-- Iteration Header -->
                <tr v-if="index === 0 || step.iteration !== relaxationStepsList[index - 1].iteration" 
                    class="iteration-group-header">
                  <td colspan="4">Iteration {{ step.iteration + 1 }}</td>
                </tr>
                
                <!-- Relaxation Step Row -->
                <tr :class="{ 
                      'current-step': index === currentStepIndex, 
                      'not-executed': index > currentStepIndex,
                      'relaxed-row': step.relaxed && index <= currentStepIndex
                    }">
                  <td class="text-muted">{{ index + 1 }}</td>
                  <td class="node-id-cell">{{ step.sourceId }}→{{ step.targetId }}</td>
                  <td class="dist-cell" :class="{ 'text-relaxed': step.relaxed && index <= currentStepIndex }">
                    {{ step.newDistance === 1.7976931348623157e308 ? '∞' : step.newDistance.toFixed(1) }}
                  </td>
                  <td style="text-align: center;">
                    <i v-if="step.relaxed" class="bi bi-check-circle-fill text-success"></i>
                    <i v-else class="bi bi-dot text-muted"></i>
                  </td>
                </tr>
              </template>

              <!-- Convergence Message -->
              <tr v-if="relaxationStepsList.length > 0 && currentStepIndex >= relaxationStepsList.length - 1" class="convergence-row">
                <td colspan="4" class="text-center">
                  <div class="convergence-message">
                    <i class="bi bi-info-circle-fill"></i>
                    Algorithm converged at Iteration {{ relaxationStepsList[relaxationStepsList.length - 1].iteration + 1 }}.
                    <span v-if="relaxationStepsList[relaxationStepsList.length - 1].iteration + 1 < (routeData?.totalVertices || 0)">
                      Early break triggered (no more updates possible).
                    </span>
                  </div>
                </td>
              </tr>

              <tr v-if="relaxationStepsList.length === 0">
                <td colspan="4" class="table-empty">Generate route to see steps</td>
              </tr>
            </tbody>
          </table>
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

// Right Panel View State
const rightPanelView = ref('status') // 'status' or 'algorithm'

// Algorithm Animation Control
const playbackState = ref('stopped') // 'playing', 'paused', 'stopped'
const playbackSpeed = ref(500) // ms delay (default to Slow)
const currentStepIndex = ref(0)
const relaxationStepsList = ref([])
const tableContainer = ref(null)

const loadPanelState = () => {
  const leftSaved = localStorage.getItem('ara_left_panel_open')
  const rightSaved = localStorage.getItem('ara_right_panel_open')
  const viewSaved = localStorage.getItem('ara_right_panel_view')
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

  rightPanelView.value = viewSaved || 'status'

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
  localStorage.setItem('ara_right_panel_view', rightPanelView.value)
}

watch(rightPanelView, () => {
  savePanelState()
})

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

const visualizeAlgorithm = async (steps) => {
  relaxationStepsList.value = steps.slice(0, 500) // Cap for performance
  currentStepIndex.value = 0
  visitedNodes.value = []
  playbackState.value = 'playing'
  
  // Switch to algorithm view in right panel and ensure it's open
  rightPanelView.value = 'algorithm'
  isRightPanelOpen.value = true
  
  await runAnimation()
}

const togglePlayback = () => {
  if (playbackState.value === 'playing') {
    playbackState.value = 'paused'
  } else {
    playbackState.value = 'playing'
    if (relaxationStepsList.value.length > 0) {
      runAnimation()
    }
  }
}

const stopPlayback = () => {
  playbackState.value = 'stopped'
  currentStepIndex.value = 0
  currentTraversedNode.value = null
  currentRelaxedEdge.value = null
  visitedNodes.value = []
}

const restartPlayback = () => {
  stopPlayback()
  // Brief delay to ensure state reset before restarting
  setTimeout(() => {
    togglePlayback()
  }, 10)
}

// Helper to auto-scroll the table to the current step
const scrollToCurrentStep = () => {
  if (!tableContainer.value) return
  const currentTR = tableContainer.value.querySelector('.current-step')
  if (currentTR) {
    currentTR.scrollIntoView({ block: 'center', behavior: 'smooth' })
  }
}

const runAnimation = async () => {
  const visitedSet = new Set(visitedNodes.value)
  
  while (playbackState.value === 'playing' && currentStepIndex.value < relaxationStepsList.value.length) {
    const step = relaxationStepsList.value[currentStepIndex.value]
    
    currentTraversedNode.value = step.targetId
    currentRelaxedEdge.value = { sourceId: step.sourceId, targetId: step.targetId }
    
    if (!visitedSet.has(step.targetId)) {
      visitedSet.add(step.targetId)
      // Only update visitedNodes occasionally or at key steps
      if (currentStepIndex.value % 5 === 0 || currentStepIndex.value === relaxationStepsList.value.length - 1) {
        visitedNodes.value = Array.from(visitedSet)
      }
    }

    scrollToCurrentStep()
    
    await new Promise(resolve => setTimeout(resolve, playbackSpeed.value))
    
    if (playbackState.value === 'playing') {
      currentStepIndex.value++
    }
  }
  
  if (currentStepIndex.value >= relaxationStepsList.value.length) {
    playbackState.value = 'stopped'
    currentTraversedNode.value = null
    currentRelaxedEdge.value = null
  }
}

// Real-time ambulance data from backend
const ambulances = ref([])

const patientName = ref('')
const emergencyType = ref('')
const selectedHospital = ref('') // Internal source, usually inferred from ambulance
const selectedAmbulance = ref('')
const destinationId = ref('')

const isDeployed = ref(false)

const getSelectedAmbulanceStatusClass = computed(() => {
  if (!selectedAmbulance.value) return ''
  const amb = ambulances.value.find(a => a.id === selectedAmbulance.value)
  if (!amb) return ''
  switch (amb.status) {
    case 'AVAILABLE': return 'text-available'
    case 'RESERVED': return 'text-reserved'
    case 'EN_ROUTE':
    case 'TRANSPORT': return 'text-enroute'
    default: return 'text-busy'
  }
})

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


  const currentAction = computed(() => {
    if (loading.value) return 'Calculating route'
    if (isDeployed.value) return 'Mission Deployed'

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
  if (isDeployed.value) return
  destinationId.value = id
  systemLog.value = `Destination set to: ${id}`
}

const handleCalculateRoute = async () => {
  if (!patientName.value || !emergencyType.value || !selectedHospital.value || !destinationId.value || !selectedAmbulance.value) {
    systemLog.value = 'Please fill in all fields before generating the route.'
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
    
    // --- OSM Road Snapping (CURVY & FLEXIBLE) ---
    if (data.coordinates && data.coordinates.length >= 2) {
      try {
        systemLog.value = 'Generating high-resolution road path...'
        const coordString = data.coordinates
          .map(c => `${c.lng || c.longitude},${c.lat || c.latitude}`)
          .join(';')
        
        const osrmRes = await fetch(`https://router.project-osrm.org/route/v1/driving/${coordString}?overview=full&geometries=geojson&continue_straight=false`)
        const osrmData = await osrmRes.json()
        
        if (osrmData.code === 'Ok' && osrmData.routes && osrmData.routes.length > 0) {
          data.coordinates = osrmData.routes[0].geometry.coordinates.map(c => ({
            lat: c[1],
            lng: c[0]
          }))
        }
      } catch (osrmErr) {
        console.error('OSM Road Generation failed:', osrmErr)
      }
    }
    
    routeData.value = data
    systemLog.value = data.message

    if (data.relaxationSteps && data.relaxationSteps.length > 0) {
      visualizeAlgorithm(data.relaxationSteps) // Run in background
    }
  } catch (err) {
    systemLog.value = 'Route calculation failed'
  } finally {
    loading.value = false
  }
}

const handleDispatch = async () => {
  if (!selectedAmbulance.value || !routeData.value) return

  const sourceNode = allNodes.value.find(n => n.id === selectedHospital.value)
  const destNode = allNodes.value.find(n => n.id === destinationId.value)

  const mission = {
    patient: {
      name: patientName.value,
      emergencyType: emergencyType.value,
      contactNumber: '' // Optional: could add an input for this
    },
    ambulance: { id: selectedAmbulance.value },
    dispatcher: { id: user.value?.id || 1 },
    startLat: sourceNode?.latitude,
    startLng: sourceNode?.longitude,
    endLat: destNode?.latitude,
    endLng: destNode?.longitude,
    estimatedTime: routeData.value.estimatedTime,
    pathJson: JSON.stringify(routeData.value.coordinates)
  }

  try {
    const res = await fetch('http://localhost:8081/api/missions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(mission),
    })

    if (res.ok) {
      systemLog.value = 'Mission dispatched! Waiting for driver confirmation.'
      isDeployed.value = true
      fetchAmbulances()
    } else {
      systemLog.value = 'Mission dispatch failed'
    }
  } catch (err) {
    systemLog.value = 'Network error during dispatch'
    console.error(err)
  }
}

const resetForm = () => {
  patientName.value = ''
  emergencyType.value = ''
  selectedHospital.value = ''
  selectedAmbulance.value = ''
  destinationId.value = ''
  routeData.value = null
  visitedNodes.value = []
  isDeployed.value = false
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
  margin-bottom: 0.85rem;
}

.form-group-large {
  margin-bottom: 1.25rem;
}

.form-label {
  display: block;
  font-size: 0.725rem;
  font-weight: 700;
  color: #4b5563;
  margin-bottom: 0.4rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: color 0.2s ease;
}

.form-input,
.form-select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 0.9rem;
  font-family: inherit;
  box-sizing: border-box;
  background-color: #ffffff;
  color: #111827;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.form-input::placeholder {
  color: #9ca3af;
  opacity: 0.8;
}

.form-input:hover,
.form-select:hover:not(:disabled) {
  border-color: #d1d5db;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  background-color: #ffffff;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1), 0 2px 4px rgba(0, 0, 0, 0.05);
}

.form-input:disabled,
.form-select:disabled {
  background-color: #f9fafb;
  color: #6b7280;
  border-color: #f3f4f6;
  cursor: not-allowed;
  opacity: 0.8;
  box-shadow: none;
}

/* Specific styling for the datalist input to match selects */
.select-like-input {
  cursor: text;
}

.is-deployed .form-label {
  color: #9ca3af;
}

.is-deployed .form-input,
.is-deployed .form-select {
  background-color: #f3f4f6;
  border-color: #e5e7eb;
}

.form-select-wrapper {
  position: relative;
}

.select-chevron {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #6b7280;
}

select.form-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23374151' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  padding-right: 2.5rem;
}

.text-available { color: #10b981 !important; }
.text-reserved { color: #ef4444 !important; }
.text-enroute { color: #f59e0b !important; }
.text-busy { color: #9ca3af !important; }

.option-available { color: #10b981; }
.option-reserved { color: #ef4444; }
.option-enroute { color: #f59e0b; }
.option-busy { color: #9ca3af; }

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

/* Right Panel View Switching */
.panel-tabs {
  display: flex;
  background-color: #f3f4f6;
  padding: 0.25rem;
  border-radius: 8px;
  margin: 0 1rem 1rem 1rem;
  gap: 0.25rem;
}

.tab-btn {
  flex: 1;
  padding: 0.5rem;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #374151;
  background-color: rgba(255, 255, 255, 0.5);
}

.tab-btn.active {
  background-color: #ffffff;
  color: #3b82f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tab-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Algorithm Tab Specifics */
.algorithm-tab {
  padding: 0 1rem;
}

.algorithm-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  gap: 0.5rem;
}

.playback-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.control-btn {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #374151;
  transition: all 0.2s;
}

.control-btn:hover {
  background-color: #f3f4f6;
  border-color: #9ca3af;
}

.speed-select-mini {
  padding: 0.2rem 0.4rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: white;
  font-size: 0.75rem;
  cursor: pointer;
}

.step-progress-mini {
  font-size: 0.75rem;
  font-weight: 700;
  color: #3b82f6;
  background: #eff6ff;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.progress-bar-container {
  height: 4px;
  background-color: #e5e7eb;
  width: 100%;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 1rem;
}

.progress-bar-fill {
  height: 100%;
  background-color: #3b82f6;
  transition: width 0.2s ease-out;
}

/* Final Path Summary */
.final-path-summary {
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 1rem;
}

.summary-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 700;
  color: #166534;
  text-transform: uppercase;
  margin-bottom: 0.4rem;
}

.path-sequence {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.4rem;
}

.path-node {
  font-family: monospace;
  font-weight: 700;
  font-size: 0.85rem;
  color: #15803d;
  background: white;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  border: 1px solid #dcfce7;
}

.path-node-cost {
  font-size: 0.7rem;
  font-weight: 600;
  color: #16a34a;
  background-color: #f0fdf4;
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  margin-left: -0.2rem;
  z-index: 1;
}

.path-arrow {
  color: #86efac;
  font-size: 0.75rem;
}

/* Convergence Row */
.convergence-row {
  background-color: #f8fafc !important;
}

.convergence-message {
  padding: 1rem;
  font-size: 0.75rem;
  color: #475569;
  line-height: 1.4;
}

.convergence-message i {
  color: #3b82f6;
  margin-right: 0.4rem;
}

.table-container-right {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
}

.table-mini {
  font-size: 0.75rem !important;
}

.relaxation-table {
  width: 100%;
  border-collapse: collapse;
}

.relaxation-table th {
  position: sticky;
  top: 0;
  background-color: #f9fafb;
  color: #4b5563;
  font-weight: 600;
  text-align: left;
  padding: 0.5rem;
  border-bottom: 2px solid #e5e7eb;
  z-index: 10;
}

.relaxation-table td {
  padding: 0.4rem 0.5rem;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

.node-id-cell {
  font-family: monospace;
  font-weight: 600;
  color: #1f2937;
}

.dist-cell {
  font-weight: 600;
}

.text-relaxed {
  color: #2563eb;
}

.relaxation-table tr.current-step {
  background-color: #eff6ff;
  border-left: 3px solid #3b82f6;
}

.relaxation-table tr.relaxed-row {
  background-color: #f0fdf4;
}

.relaxation-table tr.not-executed {
  opacity: 0.4;
}

.iteration-group-header {
  background-color: #f3f4f6;
  border-top: 1px solid #d1d5db;
}

.iteration-group-header td {
  font-weight: 700;
  color: #4b5563;
  padding: 0.4rem 0.75rem;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.text-success { color: #10b981; }
.text-muted { color: #9ca3af; }

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

.status-reserved {
  background-color: #ef4444;
}

.status-enroute {
  background-color: #f59e0b;
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
