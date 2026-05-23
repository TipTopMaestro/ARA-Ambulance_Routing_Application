<template>
  <div class="mission-logs">
    <div class="logs-container">
      <!-- Header Section -->
      <div class="page-header">
        <div class="title-section">
          <h1 class="page-title">Mission Audit Logs</h1>
          <p class="page-description">Detailed history of all ambulance dispatches and mission ETAs.</p>
        </div>
        <div class="action-section">
          <button @click="fetchLogs" class="refresh-btn" :class="{ 'is-loading': loading }">
            <i class="bi" :class="loading ? 'bi-arrow-repeat spin' : 'bi-arrow-clockwise'"></i>
            <span>Refresh Data</span>
          </button>
        </div>
      </div>

      <!-- Stats Overview -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon completed"><i class="bi bi-check-circle"></i></div>
          <div class="stat-info">
            <span class="stat-label">Completed</span>
            <span class="stat-value">{{ stats.completed }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon active"><i class="bi bi-geo-alt"></i></div>
          <div class="stat-info">
            <span class="stat-label">Active/En Route</span>
            <span class="stat-value">{{ stats.active }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon cancelled"><i class="bi bi-x-circle"></i></div>
          <div class="stat-info">
            <span class="stat-label">Cancelled</span>
            <span class="stat-value">{{ stats.cancelled }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon total"><i class="bi bi-clipboard-data"></i></div>
          <div class="stat-info">
            <span class="stat-label">Total Missions</span>
            <span class="stat-value">{{ logs.length }}</span>
          </div>
        </div>
      </div>

      <!-- Filters Section -->
      <div class="filter-bar">
        <div class="search-input">
          <i class="bi bi-search"></i>
          <input type="text" v-model="searchQuery" placeholder="Search patient name or emergency type...">
        </div>
        <div class="filter-group">
          <select v-model="statusFilter" class="filter-select">
            <option value="ALL">All Statuses</option>
            <option value="PENDING_CONFIRMATION">Pending</option>
            <option value="EN_ROUTE">En Route</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      <!-- Main Logs Card -->
      <div class="logs-card">
        <div class="table-wrapper">
          <table class="logs-table">
            <thead>
              <tr>
                <th>Mission ID</th>
                <th>Dispatch Time</th>
                <th>Patient Details</th>
                <th>Emergency</th>
                <th>Status</th>
                <th>Unit</th>
                <th>Outcome</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in filteredLogs" :key="log.id || log.missionId" class="log-row">
                <td class="id-cell">#{{ log.missionId || log.id }}</td>
                <td class="time-cell">
                  <div class="time-main">{{ formatDate(log.dispatchTime) }}</div>
                  <div class="time-relative">{{ getRelativeTime(log.dispatchTime) }}</div>
                </td>
                <td class="patient-cell">
                  <div class="patient-name">{{ log.patient?.name || 'Unknown Patient' }}</div>
                  <div class="patient-contact" v-if="log.patient?.contactNumber">{{ log.patient.contactNumber }}</div>
                </td>
                <td class="emergency-cell">
                  <span :class="['emergency-badge', getEmergencyClass(log.patient?.emergencyType)]">
                    {{ log.patient?.emergencyType || 'General' }}
                  </span>
                </td>
                <td class="status-cell">
                  <span :class="['status-pill', log.status?.toLowerCase()]">
                    <span class="status-dot"></span>
                    {{ formatStatus(log.status) }}
                  </span>
                </td>
                <td class="unit-cell">
                  <div class="unit-box">
                    <i class="bi bi-ambulance"></i>
                    <span>Unit {{ log.ambulanceId || log.ambulance?.id || 'N/A' }}</span>
                  </div>
                </td>
                <td class="outcome-cell">
                  <div v-if="log.status === 'COMPLETED' || log.status === 'ARRIVED' || log.status === 'AVAILABLE'" class="outcome-success">
                    <span class="outcome-label">Arrived:</span>
                    <span class="outcome-time">{{ formatDate(log.arrivalTime) }}</span>
                  </div>
                  <div v-else-if="log.status === 'CANCELLED' || log.status === 'FAILED'" class="outcome-cancelled">
                    <span class="outcome-label">N/A</span>
                  </div>
                  <div v-else class="outcome-pending">
                    <span class="eta-value">{{ log.estimatedTime ? log.estimatedTime.toFixed(1) + 'm' : '---' }}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          
          <!-- Empty State -->
          <div v-if="filteredLogs.length === 0" class="empty-state">
            <div class="empty-icon"><i class="bi bi-inbox"></i></div>
            <h3>No missions found</h3>
            <p v-if="searchQuery || statusFilter !== 'ALL'">Try adjusting your filters to find what you're looking for.</p>
            <p v-else>There are no recorded missions in the system yet.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const logs = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('ALL')

const stats = computed(() => {
  return {
    completed: logs.value.filter(l => l.status === 'COMPLETED' || l.status === 'AVAILABLE').length,
    active: logs.value.filter(l => l.status === 'EN_ROUTE' || l.status === 'TRANSPORT' || l.status === 'RESERVED').length,
    cancelled: logs.value.filter(l => l.status === 'CANCELLED').length
  }
})

const filteredLogs = computed(() => {
  let result = logs.value

  if (statusFilter.value !== 'ALL') {
    result = result.filter(l => l.status === statusFilter.value)
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(l => 
      (l.patient?.name?.toLowerCase().includes(query)) ||
      (l.patient?.emergencyType?.toLowerCase().includes(query)) ||
      (l.status?.toLowerCase().includes(query)) ||
      (String(l.missionId || l.id).includes(query))
    )
  }

  // Sort by time descending (newest first)
  return [...result].sort((a, b) => new Date(b.dispatchTime) - new Date(a.dispatchTime))
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8081/api/missions')
    if (res.ok) {
      logs.value = await res.json()
    }
  } catch (err) {
    console.error('Failed to fetch logs', err)
  } finally {
    loading.value = false
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

const getRelativeTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffInSeconds = Math.floor((now - date) / 1000)
  
  if (diffInSeconds < 60) return 'Just now'
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`
  return `${Math.floor(diffInSeconds / 86400)}d ago`
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
</script>

<style scoped>
.mission-logs {
  padding: 2rem;
  background-color: #f8fafc;
  min-height: calc(100vh - 80px);
  font-family: 'Inter', -apple-system, sans-serif;
}

.logs-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Header Section */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 1.875rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.025em;
}

.page-description {
  color: #64748b;
  margin-top: 0.25rem;
  font-size: 0.95rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  border: 1px solid #e2e8f0;
  padding: 0.625rem 1.25rem;
  border-radius: 8px;
  color: #334155;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.refresh-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.refresh-btn.is-loading {
  opacity: 0.7;
  cursor: wait;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.25rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.25rem;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.stat-icon.completed { background: #dcfce7; color: #16a34a; }
.stat-icon.active { background: #fffbeb; color: #f59e0b; }
.stat-icon.cancelled { background: #fee2e2; color: #dc2626; }
.stat-icon.total { background: #f1f5f9; color: #475569; }

.stat-label {
  display: block;
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 300px;
  position: relative;
}

.search-input i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  z-index: 2;
  pointer-events: none;
}

.search-input input {
  width: 100%;
  padding: 0.625rem 1rem 0.625rem 2.75rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.2s;
  position: relative;
  z-index: 1;
}

.search-input input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-select {
  padding: 0.625rem 2rem 0.625rem 1rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #334155;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 1rem;
}

/* Logs Card & Table */
.logs-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.logs-table {
  width: 100%;
  border-collapse: collapse;
}

.logs-table th {
  background: #f8fafc;
  padding: 0.875rem 1.5rem;
  text-align: left;
  font-size: 0.75rem;
  font-weight: 700;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #f1f5f9;
}

.log-row {
  transition: background 0.1s;
  border-bottom: 1px solid #f1f5f9;
}

.log-row:hover {
  background: #f8fafc;
}

.log-row td {
  padding: 1rem 1.5rem;
  vertical-align: middle;
}

.id-cell {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.8rem;
  color: #64748b;
  font-weight: 600;
}

.time-main {
  font-weight: 600;
  color: #334155;
  font-size: 0.875rem;
}

.time-relative {
  font-size: 0.75rem;
  color: #94a3b8;
}

.patient-name {
  font-weight: 700;
  color: #0f172a;
  font-size: 0.935rem;
}

.patient-contact {
  font-size: 0.75rem;
  color: #64748b;
  margin-top: 0.125rem;
}

.emergency-badge {
  display: inline-flex;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
}

.emergency-badge.danger { background: #fef2f2; color: #dc2626; border: 1px solid #fee2e2; }
.emergency-badge.warning { background: #fffbeb; color: #d97706; border: 1px solid #fef3c7; }
.emergency-badge.info { background: #eff6ff; color: #2563eb; border: 1px solid #dbeafe; }

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.025em;
  text-transform: uppercase;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-pill.reserved { background: #fef2f2; color: #ef4444; }
.status-pill.en_route, .status-pill.transport { background: #fffbeb; color: #f59e0b; }
.status-pill.completed, .status-pill.available, .status-pill.arrived { background: #f0fdf4; color: #10b981; }
.status-pill.cancelled, .status-pill.failed { background: #f1f5f9; color: #94a3b8; }

.unit-box {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #f1f5f9;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  color: #475569;
  font-weight: 600;
  font-size: 0.8rem;
}

.ETA-cell {
  font-size: 0.875rem;
}

.ETA-label { color: #94a3b8; font-size: 0.75rem; margin-right: 0.25rem; }
.ETA-time { font-weight: 600; color: #334155; }
.ETA-cancelled { color: #94a3b8; font-style: italic; }
.eta-value { font-weight: 700; color: #2563eb; }

/* Empty State */
.empty-state {
  padding: 4rem 2rem;
  text-align: center;
  color: #64748b;
}

.empty-icon {
  font-size: 3rem;
  color: #e2e8f0;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin: 0;
  color: #0f172a;
  font-size: 1.125rem;
}

.empty-state p {
  margin: 0.5rem 0 0;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .mission-logs { padding: 1rem; }
  .page-header { flex-direction: column; gap: 1rem; }
  .search-input { min-width: 100%; }
}
</style>




