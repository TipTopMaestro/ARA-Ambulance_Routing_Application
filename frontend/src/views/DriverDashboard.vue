<template>
  <div class="driver-dashboard">

    <!-- LEFT SIDE: Mission List & Actions -->
    <div class="left-panel">
      <div class="mission-card">
        <div class="card-header">
          <div>
            <h2>Driver Dashboard</h2>
            <p class="subheading">Active Missions</p>
          </div>
        </div>

        <div class="mission-list">
          <template v-if="loading">
            <div class="mission-empty">Loading missions...</div>
          </template>

          <template v-else-if="driverMissions.length === 0">
            <div class="mission-empty">No active missions assigned.</div>
          </template>

          <template v-else>
            <button
              v-for="mission in driverMissions"
              :key="mission.id"
              class="mission-item"
              :class="{ selected: selectedMission?.id === mission.id }"
              @click="selectMission(mission)"
            >
              <div>
                <div class="mission-name">{{ mission.patient?.name || 'Unknown Patient' }}</div>
                <div class="mission-meta">{{ mission.patient?.emergencyType || 'Emergency' }}</div>
              </div>
              <span class="mission-status">{{ mission.status.replace('_', ' ') }}</span>
            </button>
          </template>
        </div>
      </div>

      <!-- Action Panel: Confirmation Steps -->
      <div v-if="selectedMission" class="action-card">
        <h3 class="panel-subtitle">Mission Actions</h3>
        
        <div v-if="selectedMission.status === 'PENDING_CONFIRMATION'" class="confirmation-flow">
          <p class="instruction">New dispatch request received. Please confirm to start navigation.</p>
          <button @click="updateStatus('EN_ROUTE')" class="btn-confirm" :disabled="actionLoading">
            CONFIRM & ACCEPT
          </button>
        </div>

        <div v-else-if="selectedMission.status === 'EN_ROUTE'" class="confirmation-flow">
          <p class="instruction">You are currently on mission. Mark as completed once the patient has been handled.</p>
          <button @click="confirmCompletion" class="btn-complete" :disabled="actionLoading">
            MARK AS COMPLETED
          </button>
        </div>
      </div>

      <!-- Selected Mission Details -->
      <div v-if="selectedMission" class="details-card">
        <h3 class="panel-subtitle">Patient Details</h3>
        <div class="detail-item">
          <span class="detail-label">Name:</span>
          <span class="detail-value">{{ selectedMission.patient?.name }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Contact:</span>
          <span class="detail-value">{{ selectedMission.patient?.contactNumber || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Emergency:</span>
          <span class="detail-value highlight">{{ selectedMission.patient?.emergencyType }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">ETA:</span>
          <span class="detail-value">{{ selectedMission.estimatedTime?.toFixed(1) }} mins</span>
        </div>
      </div>
    </div>

    <!-- RIGHT SIDE: Map View -->
    <div class="right-panel">
      <div class="map-wrapper">
        <Map
          :allNodes="allNodes"
          :pathCoordinates="routeCoordinates"
          :showMapNodes="false"
          :showGraphEdges="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import Map from '../components/Map.vue'
import { useAuth } from '../composables/useAuth'

const { user } = useAuth()

const missions = ref([])
const selectedMission = ref(null)
const allNodes = ref([])
const loading = ref(true)
const actionLoading = ref(false)

let pollInterval = null

const driverMissions = computed(() => {
  if (!user.value) return []
  return missions.value.filter(
    (mission) =>
      mission.driver?.id === user.value.id &&
      ['PENDING_CONFIRMATION', 'EN_ROUTE'].includes(mission.status)
  )
})

const selectMission = (mission) => {
  selectedMission.value = mission
}

const fetchNodes = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/nodes')
    if (res.ok) allNodes.value = await res.json()
  } catch (e) { console.error("Failed to fetch nodes", e) }
}

const fetchMissions = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/missions')
    if (res.ok) {
      const data = await res.json()
      missions.value = data
      
      // Keep selection if it still exists in active missions, otherwise auto-select first
      if (selectedMission.value) {
        const stillActive = driverMissions.value.find(m => m.id === selectedMission.value.id)
        if (stillActive) {
          selectedMission.value = stillActive
        } else {
          selectedMission.value = driverMissions.value[0] || null
        }
      } else {
        selectedMission.value = driverMissions.value[0] || null
      }
    }
  } catch (err) {
    console.error('Failed to fetch missions', err)
  } finally {
    loading.value = false
  }
}

const updateStatus = async (newStatus) => {
  if (!selectedMission.value) return
  
  actionLoading.value = true
  try {
    const res = await fetch(`http://localhost:8081/api/missions/${selectedMission.value.id}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    })
    
    if (res.ok) {
      await fetchMissions()
    } else {
      alert("Failed to update status")
    }
  } catch (err) {
    console.error("Error updating status", err)
  } finally {
    actionLoading.value = false
  }
}

const confirmCompletion = () => {
  if (confirm("Are you sure you have completed this mission?")) {
    updateStatus('COMPLETED')
  }
}

onMounted(() => {
  fetchNodes()
  fetchMissions()
  pollInterval = setInterval(fetchMissions, 5000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

const routeCoordinates = computed(() => {
  if (!selectedMission.value || !selectedMission.value.pathJson) {
    return null
  }

  try {
    // Backend returns it as a JSON string via @JdbcTypeCode(SqlTypes.JSON)
    return typeof selectedMission.value.pathJson === 'string' 
      ? JSON.parse(selectedMission.value.pathJson) 
      : selectedMission.value.pathJson
  } catch (e) {
    console.error('Failed to parse route coordinates', e)
    return null
  }
})

watch(driverMissions, (missionsList) => {
  if (!selectedMission.value && missionsList.length > 0) {
    selectedMission.value = missionsList[0]
  }
})
</script>

<style scoped>
.driver-dashboard {
  padding: 1rem;
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 1rem;
  height: calc(100vh - 80px);
  background: #f3f4f6;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-y: auto;
}

.mission-card, .action-card, .details-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  padding: 1.25rem;
}

.panel-subtitle {
  font-size: 1rem;
  font-weight: 700;
  color: #374151;
  margin-bottom: 1rem;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 0.5rem;
}

.mission-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.mission-item {
  width: 100%;
  text-align: left;
  padding: 1rem;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mission-item:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.mission-item.selected {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.mission-name {
  font-weight: 700;
  color: #111827;
}

.mission-meta {
  font-size: 0.85rem;
  color: #6b7280;
}

.mission-status {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  background: #fef2f2;
  color: #991b1b;
}

.instruction {
  font-size: 0.9rem;
  color: #4b5563;
  line-height: 1.5;
  margin-bottom: 1.25rem;
}

.btn-confirm, .btn-complete {
  width: 100%;
  padding: 0.85rem;
  border-radius: 8px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-confirm {
  background-color: #3b82f6;
  color: white;
}

.btn-complete {
  background-color: #10b981;
  color: white;
}

.btn-confirm:hover, .btn-complete:hover {
  opacity: 0.9;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.detail-label {
  color: #6b7280;
}

.detail-value {
  color: #111827;
  font-weight: 600;
}

.highlight {
  color: #ef4444;
}

.right-panel {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.map-wrapper {
  height: 100%;
}

.mission-empty {
  text-align: center;
  color: #9ca3af;
  padding: 2rem 0;
  font-style: italic;
}
</style>
