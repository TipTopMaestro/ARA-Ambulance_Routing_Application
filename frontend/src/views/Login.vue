<template>
  <div class="login-page">
    <!-- Background Overlay -->
    <div class="overlay"></div>

    <!-- Login Container -->
    <div class="login-container">

      <!-- Left Side -->
      <div class="left-panel">
        <div class="logo-wrapper">
          <img
            src="/src/assets/ARA_logo.svg"
            alt="ambulance"
            class="logo"
          />
        </div>
      </div>

      <!-- Right Side -->
      <div class="right-panel">
        <h1>WELCOME</h1>
        <p class="subtitle">Log in to your account to continue</p>

        <form class="login-form" @submit.prevent="handleSubmit">
          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <div class="input-group">
            <span class="icon" aria-hidden="true">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="7" r="4"></circle>
                <path d="M5.5 21a6.5 6.5 0 0 1 13 0"></path>
              </svg>
            </span>
            <input type="text" placeholder="username" v-model="username" required />
          </div>

          <div class="input-group">
            <span class="icon" aria-hidden="true">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </span>
            <input :type="showPassword ? 'text' : 'password'" placeholder="password" v-model="password" required />
            <span class="password-toggle" @click="showPassword = !showPassword" :title="showPassword ? 'Hide password' : 'Show password'">
              <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-5 0-9.27-3-11-8 1.01-2.66 2.79-4.85 4.94-6.19"></path>
                <path d="M1 1l22 22"></path>
              </svg>
            </span>
          </div>

          <div class="forgot">
            <a href="#">forgot password?</a>
          </div>

          <button type="submit" class="login-btn">
            Log In
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const error = ref('')
const router = useRouter()
const { login } = useAuth()

const handleSubmit = async () => {
  error.value = ''
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

<style scoped>
/* Whole Page */
.login-page {
  width: 100%;
  height: 100vh;
  background: url('/src/assets/BG.avif') center center/cover no-repeat;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  font-family: Arial, Helvetica, sans-serif;
}

/* Dark Red Overlay */
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(120, 0, 0, 0.45);
}

/* Main Box */
.login-container {
  width: 720px;
  height: 420px;
  background: white;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  position: relative;
  z-index: 2;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);
}

/* Left Side */
.left-panel {
  width: 45%;
  background: #731111;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.logo-wrapper {
  text-align: center;
}

.logo {
  width: 180px;
  margin-bottom:5px;
  filter: brightness(0) invert(1);
}

.logo-wrapper h3 {
  margin: 0;
  font-size: 22px;
  font-weight: bold;
}

.logo-wrapper p {
  margin: 0;
  font-size: 15px;
}

/* Right Side */
.right-panel {
  width: 55%;
  background: #f8f8f8;
  padding: 55px 45px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.right-panel h1 {
  font-size: 42px;
  margin: 0;
  font-weight: 900;
  color: black;
  letter-spacing: 1px;
}

.subtitle {
  margin-top: 2px;
  margin-bottom: 40px;
  font-size: 12px;
  color: #555;
}

/* Form */
.login-form {
  width: 100%;
}

.icon {
  color: gray;
  font-size: 14px;
  margin-right: 8px;
}

.icon {
  filter: grayscale(100%);
}

.error-message {
  color: #731111;
  font-size: 12px;
  margin-bottom: 10px;
  text-align: center;
}

.input-group {
  width: 100%;
  height: 36px;
  border: 1px solid #bbb;
  border-radius: 18px;
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 12px;
  background: white;
}

.input-group input {
  border: none;
  outline: none;
  flex: 1;
  background: transparent;
  font-size: 14px;
}

.password-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  margin-left: 8px;
  color: #777;
  cursor: pointer;
}
.password-toggle i {
  font-size: 14px;
}

/* Forgot Password */
.forgot {
  text-align: right;
  margin-top: -4px;
  margin-bottom: 28px;
}

.forgot a {
  font-size: 10px;
  color: #444;
  text-decoration: none;
}

/* Button */
.login-btn {
  width: 120px;
  height: 36px;
  border: none;
  border-radius: 18px;
  background: #731111;
  color: white;
  font-size: 14px;
  cursor: pointer;
  display: block;
  margin: auto;
  transition: 0.3s;
}

.login-btn:hover {
  background: #6f0000;
}

/* Responsive */
@media (max-width: 768px) {
  .login-container {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .left-panel,
  .right-panel {
    width: 100%;
  }

  .left-panel {
    height: 180px;
  }
}
</style>
