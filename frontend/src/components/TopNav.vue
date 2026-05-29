<template>
  <nav class="top-nav">
    <div class="nav-container">
      <!-- Brand Section (Left) -->
      <router-link to="/" class="nav-brand">
        <div><img src="/src/assets/ara_logo.svg" alt="ARA Logo" class="logo"></div>
        <div class="Logo-title">ARA</div>
        <div class="brand-divider"></div>
        <div class="brand-subtitle">Ambulance Routing Application <br> using Bellman-Ford algorithm</div>
      </router-link>

      <!-- Navigation Links (Center) -->
      <div class="nav-links">
        <router-link to="/" class="nav-link" :class="{ 'active-link': isDashboardActive }">
          <i class="bi bi-speedometer2"></i>
          <span>Dashboard</span>
        </router-link>
        <router-link to="/logs" class="nav-link" :class="{ 'active-link': route.path === '/logs' }">
          <i class="bi bi-journal-text"></i>
          <span>Mission Logs</span>
        </router-link>
      </div>

      <!-- User Actions Section (Right) -->
      <div class="nav-user">
        <div class="user-dropdown-trigger" @click="toggleDropdown" :class="{ 'is-active': showDropdown }">
          <div class="user-avatar">
            {{ user?.username?.charAt(0).toUpperCase() || 'U' }}
          </div>
          <span class="username">{{ user?.username }}</span>
          <i class="bi bi-chevron-down chevron-icon" :class="{ 'rotate': showDropdown }"></i>
        </div>

        <transition name="dropdown">
          <div v-if="showDropdown" class="dropdown-menu">
            <div class="dropdown-header">
              <div class="header-user-info">
                <span class="header-username">{{ user?.username }}</span>
                <span class="header-role">{{ user?.role || 'User' }}</span>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <router-link to="/profile" class="dropdown-item" @click="closeDropdown">
              <i class="bi bi-person-circle"></i>
              <span>My Profile</span>
            </router-link>
            <div class="dropdown-divider"></div>
            <button @click="handleLogout" class="dropdown-item logout-btn">
              <i class="bi bi-box-arrow-right"></i>
              <span>Sign Out</span>
            </button>
          </div>
        </transition>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const { user, logout } = useAuth()
const route = useRoute()
const showDropdown = ref(false)

const isDashboardActive = computed(() => {
  return route.path === '/' || route.path.startsWith('/dispatcher') || route.path.startsWith('/driver')
})

const toggleDropdown = (e) => {
  e.stopPropagation()
  showDropdown.value = !showDropdown.value
}

const closeDropdown = () => {
  showDropdown.value = false
}

const handleLogout = () => {
  closeDropdown()
  logout()
}

onMounted(() => {
  window.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  window.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.top-nav {
  background: linear-gradient(to right, #731111, #8b1515);
  color: white;
  height: 64px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
}

.nav-container {
  width: 100%;
  padding: 0 1.5rem;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
}

/* Brand Styles */
.nav-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: white;
  transition: opacity 0.2s;
  justify-self: flex-start;
}

.nav-brand:hover {
  opacity: 0.9;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.75rem;
}

.logo {
  height: 48px;
  width: auto;
  display: block;
}

.Logo-title {
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-divider {
  width: 1px;
  height: 24px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 0.75rem;
}

.brand-subtitle {
  font-size: 0.85rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Navigation Links - Centered */
.nav-links {
  display: flex;
  gap: 0.5rem;
  justify-self: center;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
  padding: 0.6rem 1rem;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-link i {
  font-size: 1.1rem;
}

.nav-link:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.nav-link.active-link {
  color: white;
  background: rgba(255, 255, 255, 0.15);
}

/* User Dropdown - Right Aligned */
.nav-user {
  position: relative;
  justify-self: flex-end;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.user-dropdown-trigger:hover, 
.user-dropdown-trigger.is-active {
  background: rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.2);
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: white;
  color: #731111;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.85rem;
}

.username {
  font-weight: 600;
  font-size: 0.9rem;
}

.chevron-icon {
  font-size: 0.8rem;
  transition: transform 0.3s;
}

.chevron-icon.rotate {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 220px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 1001;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dropdown-header {
  padding: 1rem;
  background: #f8fafc;
}

.header-user-info {
  display: flex;
  flex-direction: column;
}

.header-username {
  color: #1e293b;
  font-weight: 700;
  font-size: 0.95rem;
}

.header-role {
  color: #64748b;
  font-size: 0.75rem;
  text-transform: uppercase;
  font-weight: 600;
  margin-top: 2px;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: #334155;
  font-size: 0.9rem;
  font-weight: 500;
  border: none;
  background: none;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
}

.dropdown-item i {
  font-size: 1.1rem;
  color: #64748b;
}

.dropdown-item:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.dropdown-item:hover i {
  color: #334155;
}

.logout-btn {
  color: #dc2626;
}

.logout-btn i {
  color: #dc2626;
}

.logout-btn:hover {
  background: #fef2f2;
  color: #b91c1c;
}

.logout-btn:hover i {
  color: #b91c1c;
}

/* Transitions */
.dropdown-enter-active, .dropdown-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .nav-container { 
    display: flex;
    justify-content: space-between;
    padding: 0 1rem; 
  }
  .brand-subtitle, .brand-divider { display: none; }
  .username { display: none; }
  .user-dropdown-trigger { padding: 4px; }
  .nav-link span { display: none; }
  .nav-link { padding: 0.6rem; }
}
</style>
