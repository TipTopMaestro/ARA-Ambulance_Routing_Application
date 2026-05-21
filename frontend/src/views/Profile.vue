<template>
  <div class="profile-view">
    <div class="profile-container">
      <div class="profile-card">
        <!-- Profile Sidebar: Identity (Left in Landscape) -->
        <div class="profile-sidebar">
          <div class="header-content">
            <div class="avatar-large">
              {{ userInitial }}
            </div>
            <div class="identity-info">
              <h1 class="display-name">{{ user?.username }}</h1>
              <div class="role-badge" :class="user?.role?.toLowerCase()">
                <i class="bi" :class="user?.role === 'DISPATCHER' ? 'bi-person-badge' : 'bi-truck'"></i>
                <span>{{ user?.role }}</span>
              </div>
            </div>
          </div>
          
          <!-- <div class="sidebar-footer">
            <button @click="$router.push('/')" class="btn-back">
              <i class="bi bi-arrow-left"></i>
              <span>Dashboard</span>
            </button>
          </div> -->
        </div>

        <!-- Profile Content: Details (Right in Landscape) -->
        <div class="profile-content">
          <div class="info-section">
            <h3 class="section-title">Account Information</h3>
            <div class="info-grid">
              <div class="info-item">
                <label class="info-label">Username</label>
                <div class="info-value">
                  <i class="bi bi-person"></i>
                  <span>{{ user?.username }}</span>
                </div>
              </div>
              <div class="info-item">
                <label class="info-label">Account Status</label>
                <div class="info-value status-active">
                  <i class="bi bi-check-circle-fill"></i>
                  <span>Active</span>
                </div>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h3 class="section-title">System Access</h3>
            <div class="info-grid">
              <div class="info-item">
                <label class="info-label">Designated Role</label>
                <div class="info-value">
                  <i class="bi bi-shield-lock"></i>
                  <span>{{ user?.role }} Control</span>
                </div>
              </div>
              <div class="info-item">
                <label class="info-label">ID</label>
                <div class="info-value">
                  <i class="bi bi-hash"></i>
                  <span>{{ user?.id || 'ARA-001' }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Informational Callout -->
          <!-- <div class="edit-callout">
            <div class="callout-icon">
              <i class="bi bi-info-circle"></i>
            </div>
            <div class="callout-content">
              <h4>Limited Access</h4>
              <p>Profile information is synchronized with the central system. Manual edits are restricted to administrative personnel only.</p>
            </div>
          </div> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuth } from '../composables/useAuth'

const { user } = useAuth()

const userInitial = computed(() => {
  return user.value?.username?.charAt(0).toUpperCase() || 'U'
})
</script>

<style scoped>
.profile-view {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.profile-container {
  width: 100%;
  max-width: 1100px;
  height: auto;
  max-height: 85vh;
}

.profile-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 50px -12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: row; /* Landscape orientation */
  min-height: 600px;
}

/* Sidebar Section (Left) */
.profile-sidebar {
  width: 350px;
  background: linear-gradient(135deg, #731111 0%, #a91c1c 100%);
  padding: 3rem 2rem;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.avatar-large {
  width: 120px;
  height: 120px;
  background: white;
  color: #731111;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3.5rem;
  font-weight: 800;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.display-name {
  margin: 0;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.025em;
  color:#e2e8f0;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50px;
  font-weight: 700;
  font-size: 0.85rem;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-transform: uppercase;
}

.sidebar-footer {
  margin-top: auto;
}

.btn-back {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.8rem 1.5rem;
  border-radius: 12px;
  color: white;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
}

.btn-back:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

/* Content Section (Right) */
.profile-content {
  flex: 1;
  padding: 3.5rem;
  overflow-y: auto;
  background: white;
}

.info-section {
  margin-bottom: 3rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.5rem 0;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f1f5f9;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.info-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.1rem;
  background: #f8fafc;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  color: #1e293b;
  font-weight: 600;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.info-value i {
  font-size: 1.1rem;
  color: #94a3b8;
}

.status-active span { color: #059669; }
.status-active i { color: #10b981; }

.edit-callout {
  display: flex;
  gap: 1.25rem;
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  padding: 1.75rem;
  border-radius: 18px;
}

.callout-icon { font-size: 1.5rem; color: #0284c7; }
.callout-content h4 { margin: 0 0 0.25rem 0; color: #0369a1; font-weight: 700; }
.callout-content p { margin: 0; color: #075985; font-size: 0.9rem; line-height: 1.6; }

/* Responsive Adjustments */
@media (max-width: 900px) {
  .profile-card { flex-direction: column; min-height: auto; }
  .profile-sidebar { width: 100%; padding: 2.5rem; }
  .profile-content { padding: 2.5rem; }
  .profile-container { max-height: none; }
}

@media (max-width: 640px) {
  .profile-view { padding: 1rem; }
  .profile-sidebar, .profile-content { padding: 2rem 1.5rem; }
  .display-name { font-size: 1.75rem; }
}
</style>
