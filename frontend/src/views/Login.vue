<template>
  <div class="login-container" style="display: flex; justify-content: center; align-items: center; height: 100vh; backgroundColor: #f3f4f6">
    <div class="card" style="width: 400px; padding: 2rem">
      <h1 style="text-align: center; margin-bottom: 1.5rem">ARA Log In</h1>
      <p v-if="error" style="color: red; text-align: center">{{ error }}</p>
      <form @submit.prevent="handleSubmit" style="display: flex; flex-direction: column; gap: 1rem">
        <div>
          <label>ID / Username</label>
          <input 
            type="text" 
            v-model="username" 
            style="width: 100%; padding: 0.8rem; border-radius: 0.5rem; border: 1px solid #d1d5db"
            required
          />
        </div>
        <div>
          <label>Password</label>
          <input 
            type="password" 
            v-model="password" 
            style="width: 100%; padding: 0.8rem; border-radius: 0.5rem; border: 1px solid #d1d5db"
            required
          />
        </div>
        <button type="submit" style="background-color: #ef4444; color: white; padding: 1rem; border: none; border-radius: 0.5rem; cursor: pointer; font-size: 1rem">
          Log In
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const username = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()
const { login } = useAuth()

const handleSubmit = async () => {
  try {
    const response = await fetch('http://localhost:8081/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value }),
    })

    if (response.ok) {
      const data = await response.json()
      login(data)
      if (data.role === 'DISPATCHER') {
        router.push('/dispatcher')
      } else {
        router.push('/driver')
      }
    } else {
      error.value = 'Invalid username or password'
    }
  } catch (err) {
    error.value = 'Connection error. Is the backend running?'
  }
}
</script>
