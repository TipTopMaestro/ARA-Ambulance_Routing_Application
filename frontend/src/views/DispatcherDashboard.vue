<template>
  <div class="dispatcher-dashboard">
    
    <!-- Left Panel: Emergency Details Form -->
    <div class="left-panel">
      <div class="panel-header">
        <h2 class="panel-title">Emergency Details</h2>
        <button @click="resetForm" class="reset-btn">Reset</button>
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
        <label class="form-label">Patient Location:</label>
        <select v-model="destinationId" class="form-select">
          <option value="">Select patient location</option>
          <option v-for="n in allNodes" :key="n.id" :value="n.id">
            {{ n.name || n.id }}
          </option>
        </select>
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
        :disabled="!selectedAmbulance || !destinationId || loading"
        class="btn-primary btn-large"
      >
        GENERATE ROUTE
      </button>

      <!-- ETA Section -->
      <div class="eta-section">
        <h3 class="eta-title">ETA</h3>
        
        <div class="eta-item">
          <label class="eta-label">Unit Station (Hospital):</label>
          <div class="eta-value">
            {{ routeData?.estimatedTime?.toFixed(1) || '0.0' }} min
          </div>
        </div>

        <div class="eta-item">
          <label class="eta-label">Patient Location:</label>
          <div class="eta-location">
            {{ destinationId ? allNodes.find(n => n.id === destinationId)?.name || destinationId : 'Not selected' }}
          </div>
        </div>

        <button 
          @click="handleDispatch"
          :disabled="!routeData || !selectedAmbulance"
          class="btn-primary btn-secondary"
        >
          Dispatch Unit {{ selectedAmbulance }}
        </button>
      </div>
    </div>

    <!-- Center Panel: Map -->
    <div class="map-panel">
      <Map 
        :allNodes="allNodes" 
        :pathCoordinates="routeData?.coordinates" 
        :onNodeClick="handleNodeClick"
        :selectedSource="selectedHospital"
        :selectedDestination="destinationId"
        :allEdges="allEdges"
        :currentTraversedNode="currentTraversedNode"
        :currentRelaxedEdge="currentRelaxedEdge"
        :visitedNodes="visitedNodes"
      />
    </div>

    <!-- Right Panel: Fleet Status & System Status -->
    <div class="right-panel">
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
import Map from '../components/Map.vue'
import { useAuth } from '../composables/useAuth'

const { user } = useAuth()
const allNodes = ref([])
const allEdges = ref([])
const ambulances = ref([])

const patientName = ref('')
const emergencyType = ref('')
const selectedHospital = ref('') // Internal source, usually inferred from ambulance
const selectedAmbulance = ref('')
const selectedHospitalId = ref(null)
const destinationId = ref('')
const locationDescription = ref('')

const routeData = ref(null)
const loading = ref(false)
const systemLog = ref('Ready')

const currentTraversedNode = ref(null)
const currentRelaxedEdge = ref(null)
const visitedNodes = ref([])
const visualizationActive = ref(false)

let pollInterval = null

onMounted(() => {
  fetchNodes()
  fetchAmbulances()
  pollInterval = setInterval(() => {
    fetchAmbulances()
  }, 5000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

const fetchNodes = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/nodes')
    if (res.ok) allNodes.value = await res.json()
  } catch (e) { console.error("Failed to fetch nodes", e) }
}

const fetchAmbulances = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/ambulances')
    if (res.ok) ambulances.value = await res.json()
  } catch (e) { console.error("Failed to fetch ambulances", e) }
}

const handleNodeClick = (id) => {
  destinationId.value = id
  systemLog.value = `Destination set to: ${id}`
}

const handleCalculateRoute = async () => {
  if (!selectedAmbulance.value || !destinationId.value) {
    systemLog.value = 'Please select an ambulance and destination location.'
    return
  }

  // Find source hospital from selected ambulance or use default H1 if none selected
  let sourceId = 'H1'
  if (selectedAmbulance.value) {
    const amb = ambulances.value.find(a => a.id === selectedAmbulance.value)
    if (amb) {
      sourceId = amb.hospitalOsmNodeId
      selectedHospitalId.value = amb.hospitalId
    }
  }
  selectedHospital.value = sourceId

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

    routeData.value = data
    systemLog.value = data.message
  } catch (err) {
    systemLog.value = 'Route calculation failed'
  } finally {
    loading.value = false
  }
}

const visualizeAlgorithm = async (steps) => {
  visualizationActive.value = true
  visitedNodes.value = []
  const visitedSet = new Set()

  // Limit steps to visualize if too many, or just go fast
  const visualizationSteps = steps.slice(0, 1000) // Slightly more for better visualization
  for (const step of visualizationSteps) {
    currentTraversedNode.value = step.targetId
    currentRelaxedEdge.value = { sourceId: step.sourceId, targetId: step.targetId }
    
    if (!visitedSet.has(step.targetId)) {
      visitedSet.add(step.targetId)
      visitedNodes.value = Array.from(visitedSet)
    }

    // Faster speed for the "wave" effect
    await new Promise(resolve => setTimeout(resolve, 5))
  }
  currentTraversedNode.value = null
  currentRelaxedEdge.value = null
  visualizationActive.value = false
}

const handleDispatch = async () => {
  if (!selectedAmbulance.value || !routeData.value) return

  if (!routeData.value.pathId) {
    systemLog.value = 'Route was calculated but not saved. Generate the path again.'
    return
  }

  const patientRes = await fetch('http://localhost:8081/api/patients', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: patientName.value,
      emergencyType: emergencyType.value,
    }),
  })

  if (!patientRes.ok) {
    systemLog.value = 'Patient record could not be saved'
    return
  }

  const patient = await patientRes.json()
  const mission = {
    patientId: patient.id,
    emergencyType: emergencyType.value,
    hospitalId: selectedHospitalId.value || ambulances.value.find(a => a.id === selectedAmbulance.value)?.hospitalId,
    ambulanceId: selectedAmbulance.value,
    dispatcherId: user.value?.id || 1,
    status: 'DISPATCHED',
    pathId: routeData.value.pathId
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
/* Main Container */
.dispatcher-dashboard {
  padding: 1rem;
  display: grid;
  grid-template-columns: 350px 1fr 300px;
  gap: 1rem;
  height: calc(100vh - 80px);
  background-color: #f3f4f6;
}

/* Card Base Style */
.card,
.left-panel,
.map-panel,
.fleet-status,
.system-status {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Left Panel */
.left-panel {
  overflow-y: auto;
  padding: 1.5rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.panel-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: bold;
  color: #374151;
}

.reset-btn {
  padding: 0.4rem 0.8rem;
  font-size: 0.75rem;
  background-color: #374151;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.reset-btn:hover:not(:disabled) {
  opacity: 0.9;
}

/* Form Elements */
.form-group {
  margin-bottom: 1rem;
}

.form-group-large {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.3rem;
}

.form-input,
.form-select {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 0.9rem;
  font-family: inherit;
  box-sizing: border-box;
  background-color: white;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #3b82f6;
}

/* Buttons */
.btn-primary {
  width: 100%;
  padding: 0.8rem;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-large {
  font-size: 1rem;
  margin-bottom: 1.5rem;
}

.btn-secondary {
  padding: 0.6rem;
  font-size: 0.9rem;
  margin-bottom: 0;
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
  padding: 1rem;
}

.eta-title {
  font-weight: bold;
  color: #374151;
  margin: 0 0 1rem 0;
  font-size: 0.95rem;
}

.eta-item {
  margin-bottom: 1rem;
}

.eta-label {
  font-size: 0.8rem;
  color: #6b7280;
  display: block;
  margin-bottom: 0.3rem;
}

.eta-value {
  font-size: 2rem;
  font-weight: bold;
  color: #374151;
}

.eta-location {
  font-size: 0.9rem;
  color: #374151;
  background-color: #f9fafb;
  padding: 0.6rem;
  border-radius: 4px;
}

/* Center Panel: Map */
.map-panel {
  padding: 0;
  position: relative;
  overflow: hidden;
}

/* Right Panel */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow: hidden;
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
</style>
