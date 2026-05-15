<template>
  <div class="mission-logs">
    <h2>Mission Logs</h2>
    <div class="card">
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
            <td>{{ log.patientName }}</td>
            <td>{{ log.emergencyType }}</td>
            <td>{{ log.status }}</td>
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
  padding: 2rem;
}
.card {
  background: white;
  padding: 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
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
