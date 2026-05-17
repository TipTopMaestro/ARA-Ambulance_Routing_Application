<template>
  <div class="profile-view">
    <div class="card">
        <h2>User Profile</h2>
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
    margin-top: 0;
  padding: 1rem;
  display: flex;
  justify-content: left;
}

.card {
    background: #fff;
    padding: 1.5rem;
    border-radius: 10px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    width: 500px;
}

.form-group {
  margin-bottom: 1rem;
  width: 475px;
}
</style>
