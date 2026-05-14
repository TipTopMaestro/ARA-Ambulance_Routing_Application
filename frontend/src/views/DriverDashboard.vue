<template>
  <div class="driver-dashboard" style="padding: 1rem; display: grid; grid-template-columns: 400px 1fr; gap: 1rem; height: calc(100vh - 2rem)">
    
    <!-- Left Panel: Mission Details & Status -->
    <div class="card" style="display: flex; flex-direction: column">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <h2>Driver Dashboard</h2>
        <button @click="logout" style="padding: 0.4rem; font-size: 0.8rem">Logout</button>
      </div>
      <p>Logged in as: <strong>{{ user.username }}</strong></p>

      <div style="flex: 1; margin-top: 1rem">
        <div v-if="activeMission" style="padding: 1.5rem; background-color: #f0fdf4; border-radius: 1rem; border: 1px solid #bbf7d0">
          <h3 style="color: #166534; margin-top: 0">ACTIVE MISSION</h3>
          <p><strong>Patient:</strong> {{ activeMission.patientName }}</p>
          <p><strong>Emergency:</strong> {{ activeMission.emergencyType }}</p>
          <p><strong>Location:</strong> {{ activeMission.patientLocationName }}</p>
          <p><strong>Hospital:</strong> {{ activeMission.hospitalId }}</p>
          <p><strong>Ambulance:</strong> {{ activeMission.ambulanceId }}</p>
          <p><strong>ETA:</strong> {{ activeMission.estimatedTime?.toFixed(1) }} mins</p>
          <hr style="margin: 1.5rem 0; border-color: #d1fae5" />
          
          <div style="display: flex; flex-direction: column; gap: 0.8rem">
            <button 
              @click="updateStatus('EN_ROUTE')" 
              :disabled="activeMission.status !== 'DISPATCHED'"
              :style="{ backgroundColor: activeMission.status === 'EN_ROUTE' ? '#10b981' : '#3b82f6' }"
            >
              {{ activeMission.status === 'EN_ROUTE' ? '✓ EN ROUTE' : 'EN ROUTE' }}
            </button>
            <button 
              @click="updateStatus('TRANSPORT')" 
              :disabled="activeMission.status !== 'EN_ROUTE'"
              :style="{ backgroundColor: activeMission.status === 'TRANSPORT' ? '#10b981' : '#f59e0b' }"
            >
              {{ activeMission.status === 'TRANSPORT' ? '✓ TRANSPORTING' : 'TRANSPORT' }}
            </button>
            <button 
              @click="updateStatus('COMPLETED')" 
              :disabled="activeMission.status !== 'TRANSPORT'"
              style="background-color: #ef4444"
            >
              COMPLETED
            </button>
          </div>
        </div>
        <div v-else-if="!loading" style="padding: 3rem; text-align: center; color: #6b7280">
          <p style="font-size: 1.2rem">No active missions.</p>
          <p>Waiting for dispatcher assignment...</p>
        </div>
        <div v-else style="padding: 2rem; text-align: center">Loading...</div>
      </div>
    </div>

    <!-- Right Panel: Navigation Map -->
    <div class="card" style="padding: 0">
      <Map 
        :allNodes="allNodes" 
        :pathCoordinates="routeCoordinates"
        :selectedSource="activeMission?.hospitalId"
        :selectedDestination="destinationId"
        :onNodeClick="() => {}"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import Map from '../components/Map.vue'
import { useAuth } from '../composables/useAuth'

const { user, logout } = useAuth()
const activeMission = ref(null)
const allNodes = ref([])
const loading = ref(true)

let pollInterval = null

onMounted(() => {
  fetchNodes()
  fetchActiveMission()
  pollInterval = setInterval(fetchActiveMission, 10000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

const fetchNodes = async () => {
  const res = await fetch('http://localhost:8080/api/nodes')
  if (res.ok) allNodes.value = await res.json()
}

const fetchActiveMission = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/missions/active/${user.id}`)
    if (res.ok) {
      activeMission.value = await res.json()
    } else {
      activeMission.value = null
    }
  } catch (err) {
    console.error('Failed to fetch active mission')
  } finally {
    loading.value = false
  }
}

const updateStatus = async (newStatus) => {
  if (!activeMission.value) return
  const res = await fetch(`http://localhost:8080/api/missions/${activeMission.value.id}/status`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: newStatus }),
  })
  if (res.ok) {
    fetchActiveMission()
  }
}

const routeCoordinates = computed(() => {
  if (!activeMission.value || !activeMission.value.routeCoordinatesJson) return null
  try {
    return JSON.parse(activeMission.value.routeCoordinatesJson)
  } catch (e) {
    console.error("Failed to parse route coordinates", e)
    return null
  }
})

const destinationId = computed(() => {
  if (!activeMission.value) return null
  return allNodes.value.find(n => n.latitude === activeMission.value.patientLat && n.longitude === activeMission.value.patientLng)?.id
})
</script>
