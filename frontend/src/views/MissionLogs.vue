<template>
  <div class="mission-logs">
    
    <div class="card">
      <h2>Mission Logs</h2>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Patient</th>
            <th>Emergency</th>
            <th>Status</th>
            <th>Ambulance</th>
            <th>ETA</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td>{{ formatDate(log.dispatchTime) }}</td>
            <td>{{ log.patient?.name || 'N/A' }}</td>
            <td>{{ log.patient?.emergencyType || 'N/A' }}</td>
            <td>{{ log.status.replace('_', ' ') }}</td>
            <td>{{ log.ambulanceId }}</td>
            <td>{{ log.estimatedTime?.toFixed(1) }}m</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const logs = ref([])

onMounted(async () => {
  const res = await fetch('http://localhost:8081/api/missions')
  if (res.ok) logs.value = await res.json()
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}
</script>

<style scoped>
.mission-logs {
  padding: 1rem;
}
.card {
  border-radius: 10px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  background-color: #ffffff;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}
</style>
