<template>
  <div class="driver-dashboard">

    <!-- LEFT SIDE -->
    <div class="left-panel">

      <div class="mission-card">
        <div class="card-header">
          <div>
            <h2>Driver Dashboard</h2>
            <p class="subheading">Assigned Missions</p>
          </div>
        </div>

        <div class="mission-list">
          <template v-if="loading">
            <div class="mission-empty">Loading missions...</div>
          </template>

          <template v-else-if="driverMissions.length === 0">
            <div class="mission-empty">No active missions yet.</div>
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
                <div class="mission-name">{{ mission.patientName || 'Unknown Patient' }}</div>
                <div class="mission-meta">{{ mission.emergencyType || 'Emergency' }}</div>
              </div>
              <span class="mission-status">{{ mission.status }}</span>
            </button>
          </template>
        </div>
      </div>
    </div>

    <!-- RIGHT SIDE -->
    <div class="right-panel">

      <!-- MAP -->
      <div class="map-wrapper">
        <Map
          :allNodes="allNodes"
          :pathCoordinates="routeCoordinates"
          :selectedSource="selectedMission?.hospitalOsmNodeId"
          :selectedDestination="destinationId"
          :onNodeClick="() => {}"
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

let pollInterval = null

const driverMissions = computed(() => {
  if (!user.value) return []
  return missions.value.filter(
    (mission) =>
      mission.driver?.id === user.value.id &&
      ['DISPATCHED', 'EN_ROUTE', 'TRANSPORT'].includes(mission.status)
  )
})

const selectMission = (mission) => {
  selectedMission.value = mission
}

const fetchNodes = async () => {
  const res = await fetch('http://localhost:8081/api/nodes')
  if (res.ok) {
    allNodes.value = await res.json()
  }
}

const fetchMissions = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/missions')
    if (res.ok) {
      missions.value = await res.json()
      if (!selectedMission.value || !driverMissions.value.some((m) => m.id === selectedMission.value.id)) {
        selectedMission.value = driverMissions.value[0] || null
      }
    }
  } catch (err) {
    console.error('Failed to fetch missions', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchNodes()
  fetchMissions()
  pollInterval = setInterval(fetchMissions, 10000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

const routeCoordinates = computed(() => {
  if (!selectedMission.value || !selectedMission.value.routeCoordinatesJson) {
    return null
  }

  try {
    return JSON.parse(selectedMission.value.routeCoordinatesJson)
  } catch (e) {
    console.error('Failed to parse route coordinates', e)
    return null
  }
})

const destinationId = computed(() => {
  if (!selectedMission.value) return null
  return allNodes.value.find(
    (n) =>
      n.latitude === selectedMission.value.patientLat &&
      n.longitude === selectedMission.value.patientLng
  )?.id
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
  grid-template-columns: 280px 1fr;
  gap: 1rem;
  height: calc(100vh - 2rem);
  background: #f5f5f5;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 280px;
}


.mission-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.card-header > div {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.subheading {
  margin: 0.25rem 0 0;
  color: #6b7280;
  font-size: 0.93rem;
}


.mission-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mission-item {
  width: 100%;
  text-align: left;
  padding: 0.9rem 1rem;
  border-radius: 14px;
  border: 1px solid #d1d5db;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.mission-item:hover,
.mission-item.selected {
  background: #fef2f2;
  border-color: #fca5a5;
}

.mission-name {
  font-weight: 600;
  margin-bottom: 0.15rem;
}

.mission-meta {
  color: #6b7280;
  font-size: 0.95rem;
}

.mission-status {
  font-size: 0.85rem;
  color: #991b1b;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.map-wrapper {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  min-height: 420px;
}

.patient-info {
  background: white;
  border-radius: 16px;
  padding: 1.25rem;
  border: 1px solid #e5e7eb;
}

.patient-info p {
  margin: 0.6rem 0;
  line-height: 1.5;
}

.patient-info strong {
  color: #111827;
}

.mission-empty {
  color: #6b7280;
  text-align: center;
  padding: 1rem;
}
</style>