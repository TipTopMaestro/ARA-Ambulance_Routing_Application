<template>
  <div class="dispatcher-dashboard" style="padding: 1rem; display: grid; grid-template-columns: 350px 1fr 300px; gap: 1rem; height: calc(100vh - 80px)">
    
    <!-- Left Panel: New Request Form -->
    <div class="card" style="overflow-y: auto">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem">
        <h2 style="margin: 0">New Request</h2>
        <button @click="resetForm" style="padding: 0.4rem; font-size: 0.8rem; background-color: #374151">Reset</button>
      </div>
      
      <div class="form-group" style="margin-bottom: 1rem">
        <label>Patient Name</label>
        <input type="text" v-model="patientName" placeholder="Enter patient name" />
      </div>
      
      <div class="form-group" style="margin-bottom: 1rem">
        <label>Emergency Type</label>
        <input type="text" v-model="emergencyType" placeholder="e.g. Cardiac Arrest, Trauma" />
      </div>

      <div class="form-group" style="margin-bottom: 1rem">
        <label>Ambulance Selection</label>
        <select v-model="selectedAmbulance">
          <option value="">Select Ambulance</option>
          <option v-for="amb in ambulances" :key="amb.id" :value="amb.id">
            Ambulance #{{ amb.id }} (Hospital {{ amb.hospitalId }}) - {{ amb.status }}
          </option>
        </select>
      </div>

      <div class="form-group" style="margin-bottom: 1rem">
        <label>Patient Location (Destination)</label>
        <select v-model="destinationId">
          <option value="">Select Location</option>
          <option v-for="n in allNodes" :key="n.id" :value="n.id">
            {{ n.name || n.id }}
          </option>
        </select>
      </div>

      <div class="form-group" style="margin-bottom: 1.5rem">
        <label>More Location Description</label>
        <textarea v-model="locationDescription" rows="3" placeholder="Additional details..."></textarea>
      </div>

      <button 
        @click="handleCalculateRoute" 
        :disabled="!selectedAmbulance || !destinationId || loading"
        style="width: 100%; margin-bottom: 1rem; background-color: #3b82f6"
      >
        Generate Optimal Path
      </button>

      <div v-if="routeData" style="padding: 1rem; background-color: #eff6ff; border-radius: 0.5rem; margin-bottom: 1rem">
        <p><strong>ETA:</strong> {{ routeData.estimatedTime?.toFixed(1) }} mins</p>
        <p style="font-size: 0.8rem; color: #6b7280">
          {{ routeData.negativeCycleDetected ? '⚠ Traffic loop detected' : '✓ Shortest path found' }}
        </p>
        <button 
          @click="handleDispatch" 
          :disabled="!selectedAmbulance"
          style="width: 100%; margin-top: 0.5rem; background-color: #ef4444"
        >
          Dispatch Unit {{ selectedAmbulance }}
        </button>
      </div>

      <div style="font-size: 0.85rem; color: #4b5563">
        <strong>System Log:</strong> {{ systemLog }}
      </div>
    </div>

    <!-- Center Panel: Map -->
    <div class="card" style="padding: 0; position: relative">
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

    <!-- Right Panel: Status -->
    <div style="display: flex; flexDirection: column; gap: 1rem; overflow: hidden">
      <div class="card" style="flex: 0 0 auto">
        <h3 style="margin-top: 0">System Status</h3>
        <div style="display: flex; flex-direction: column; gap: 0.5rem">
          <div style="display: flex; justify-content: space-between">
            <span>Ambulances Active:</span>
            <strong>{{ ambulances.filter(a => a.status !== 'AVAILABLE').length }}</strong>
          </div>
          <div style="display: flex; justify-content: space-between">
            <span>Ambulances Available:</span>
            <strong style="color: #10b981">{{ ambulances.filter(a => a.status === 'AVAILABLE').length }}</strong>
          </div>
        </div>
      </div>

      <div class="card" style="flex: 1; overflow-y: auto">
        <!-- Blank for now as requested -->
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
textarea {
  width: 100%;
  padding: 0.8rem;
  border-radius: 0.5rem;
  border: 1px solid #d1d5db;
  font-family: inherit;
  font-size: 0.9rem;
}
</style>
