<template>
  <nav class="top-nav">
    <div class="nav-brand">
      <img src="/src/assets/ARA_logo.svg" alt="ARA Logo" class="logo">
    </div>
    <div class="nav-links">
      <router-link to="/driver" class="nav-link">Dashboard</router-link>
      <router-link to="/logs" class="nav-link">Mission Logs</router-link>
    </div>
    <div class="nav-user">
      <div class="dropdown" @click="toggleDropdown">
        <span class="username">{{ displayName }}</span>
        <div v-if="showDropdown" class="dropdown-menu">
          <router-link to="/profile" class="dropdown-item">Edit Profile</router-link>
          <button @click="logout" class="dropdown-item logout-btn">Logout</button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useAuth } from '../composables/useAuth'

const { user, logout } = useAuth()
const displayName = computed(() => {
  return user.value?.username ? user.value.username.charAt(0).toUpperCase() + user.value.username.slice(1) : ''
})
const showDropdown = ref(false)

const toggleDropdown = (e) => {
  e.stopPropagation()
  showDropdown.value = !showDropdown.value
}

const closeDropdown = () => {
  showDropdown.value = false
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  background-color: #731111;
  color: white;
  height: 60px;
}
.logo{
  width:25% ;
  height:25% ;
}
.nav-brand h1 {
  font-size: 1.25rem;
  color: #fff;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: #d1d5db;
  text-decoration: none;
  font-weight: 500;
}

.nav-link.router-link-active {
  color: #fff;
  border-bottom: 2px solid #ef4444;
}

.nav-user {
  position: relative;
}

.username {
  cursor: pointer;
  color: #ffffff;
  padding: 0;
  background: none;
  border-radius: 0;
  text-decoration: none;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 0.5rem;
  background-color: white;
  color: #111827;
  border-radius: 0.5rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  min-width: 150px;
  overflow: hidden;
  z-index: 1001;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: #111827;
  text-align: left;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.9rem;
}

.dropdown-item:hover {
  background-color: #f3f4f6;
}

.logout-btn {
  color: #731111;
  border-top: 1px solid #e5e7eb;
}
</style>
