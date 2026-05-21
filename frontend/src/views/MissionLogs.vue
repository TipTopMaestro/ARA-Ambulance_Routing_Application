<template>
  <div class="mission-logs">
    
    <div class="card">
      <div class="card-header">
        <h2>Mission Logs History</h2>
        <button @click="fetchLogs" class="refresh-btn">
          <i class="bi bi-arrow-clockwise"></i> Refresh
        </button>
      </div>
      
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Patient Name</th>
            <th>Emergency Type</th>
            <th>Status</th>
            <th>Ambulance</th>
            <th>ETA</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id || log.missionId">
            <td>{{ formatDate(log.dispatchTime) }}</td>
            <td class="font-bold">{{ log.patient?.name || '---' }}</td>
            <td>
              <span :class="['emergency-badge', getEmergencyClass(log.patient?.emergencyType)]">
                {{ log.patient?.emergencyType || 'General' }}
              </span>
            </td>
            <td>
              <span :class="['status-pill', log.status?.toLowerCase()]">
                {{ formatStatus(log.status) }}
              </span>
            </td>
            <td>Unit {{ log.ambulanceId || 'N/A' }}</td>
            <td>{{ log.estimatedTime ? log.estimatedTime.toFixed(1) + 'm' : '---' }}</td>
          </tr>
          <tr v-if="logs.length === 0">
            <td colspan="6" class="text-center py-8 text-gray-500">No mission history found.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const logs = ref([])

const fetchLogs = async () => {
  try {
    const res = await fetch('http://localhost:8081/api/missions')
    if (res.ok) {
      const data = await res.json()
      console.log('Fetched logs:', data)
      logs.value = data
    }
  } catch (err) {
    console.error('Failed to fetch logs', err)
  }
}

onMounted(fetchLogs)

const formatDate = (dateStr) => {
  if (!dateStr) return '---'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString([], { 
      month: 'short', 
      day: 'numeric', 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } catch (e) {
    return dateStr
  }
}

const formatStatus = (status) => {
  if (!status) return 'Unknown'
  return status.replace(/_/g, ' ')
}

const getEmergencyClass = (type) => {
  if (!type) return ''
  const t = type.toLowerCase()
  if (t.includes('cardiac') || t.includes('heart') || t.includes('trauma')) return 'danger'
  if (t.includes('respiratory') || t.includes('breath')) return 'warning'
  return 'info'
}

const getStatusClass = (status) => {
  if (!status) return ''
  return status.toLowerCase().replace(/_/g, '-')
}
</script>

<style scoped>
.mission-logs {
  padding: 2rem;
  background-color: #f3f4f6;
  min-height: calc(100vh - 80px);
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #111827;
  font-size: 1.25rem;
  font-weight: 700;
}

.refresh-btn {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background: #e5e7eb;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background-color: #f9fafb;
  padding: 1rem;
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #e5e7eb;
}

td {
  padding: 1rem;
  vertical-align: middle;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
  font-size: 0.9rem;
}

.font-bold {
  font-weight: 600;
  color: #111827;
}

.emergency-badge {
  padding: 0.25rem 0.6rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.emergency-badge.danger { background: #fee2e2; color: #991b1b; }
.emergency-badge.warning { background: #fef3c7; color: #92400e; }
.emergency-badge.info { background: #e0f2fe; color: #075985; }

.status-pill {
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
}

.status-pill.pending_confirmation { background: #f3f4f6; color: #374151; }
.status-pill.en_route { background: #dcfce7; color: #166534; }
.status-pill.completed { background: #e0f2fe; color: #075985; }
.status-pill.cancelled { background: #fee2e2; color: #991b1b; }

.text-center { text-align: center; }
.py-8 { padding-top: 2rem; padding-bottom: 2rem; }
.text-gray-500 { color: #6b7280; }
</style>
