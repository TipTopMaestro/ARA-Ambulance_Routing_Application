<template>
  <div class="profile-view">
    <h2>User Profile</h2>
    <div class="card" style="max-width: 500px">
      <div class="form-group">
        <label>Username</label>
        <input type="text" :value="user?.username" disabled />
      </div>
      <div class="form-group">
        <label>Role</label>
        <input type="text" :value="user?.role" disabled />
      </div>
      <p style="color: #6b7280; font-size: 0.9rem; margin-top: 1rem">
        Profile editing is currently limited to view-only.
      </p>
    </div>
  </div>
</template>

<script setup>
import { useAuth } from '../composables/useAuth'
const { user } = useAuth()
</script>

<style scoped>
.profile-view {
  padding: 2rem;
}
.form-group {
  margin-bottom: 1rem;
}
</style>
