<template>
  <div class="login-page">
    <div class="overlay"></div>

    <div class="logo-section">
      <img src="/src/assets/ara_logo.svg" alt="ARA Logo" class="logo" />
    </div>

    <div class="login-arc-container">
      <div class="login-content">
        
        <div class="form-header">
          <h1>WELCOME</h1>
          <p class="subtitle">Log in to your account to continue</p>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <Transition name="fade">
            <div v-if="error" class="error-message">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ error }}
            </div>
          </Transition>

          <div class="input-field">
            <label for="username" class="sr-only">Username</label>
            <div class="input-group" :class="{ 'has-error': error && !username }">
              <span class="icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </span>
              <input 
                id="username"
                type="text" 
                placeholder="Username" 
                v-model="username" 
                required 
                :disabled="isLoading"
                autocomplete="username"
              />
            </div>
          </div>

          <div class="input-field">
            <label for="password" class="sr-only">Password</label>
            <div class="input-group" :class="{ 'has-error': error && !password }">
              <span class="icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </span>
              <input 
                id="password"
                :type="showPassword ? 'text' : 'password'" 
                placeholder="Password" 
                v-model="password" 
                required 
                :disabled="isLoading"
                autocomplete="current-password"
              />
              <button 
                type="button"
                class="password-toggle" 
                @click="showPassword = !showPassword"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
              >
                <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-5 0-9.27-3-11-8 1.01-2.66 2.79-4.85 4.94-6.19"></path>
                  <path d="M1 1l22 22"></path>
                </svg>
              </button>
            </div>
          </div>

          <div class="forgot">
            <a href="#" @click.prevent>forgot password</a>
          </div>

          <div class="submit-container">
            <button type="submit" class="login-btn" :disabled="isLoading">
              <span v-if="!isLoading">Log In</span>
              <div v-else class="loader"></div>
            </button>
          </div>
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
const isLoading = ref(false)
const error = ref('')
const router = useRouter()
const { login } = useAuth()

const handleSubmit = async () => {
  if (isLoading.value) return

  error.value = ''
  isLoading.value = true

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
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  background: url('/src/assets/BG.png') center center/cover no-repeat;
  display: flex;
  justify-content: center;
  position: relative;
  overflow: hidden; /* Important to hide the wide arc overflow */
  font-family: 'Inter', sans-serif;
  background-color: #111;
}

.overlay {
  position: absolute;
  inset: 0;
  /* Reduced red tint, matching the original darker background */
  background: rgba(0, 0, 0, 0.556); 
}

/* Floating Logo Section */
.logo-section {
  position: absolute;
  top: 15vh; /* Adjust height placement as needed */
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 50%; /* Sized to match the large logo in reference */
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.3));
}

/* The Arc Effect */
.login-arc-container {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%; /* Extremely wide to make the curve shallow */
  height: 50vh; /* Takes up lower ~65% of screen */
  background: rgba(235, 235, 235, 0.85); /* Translucent white/grey to show background */
  clip-path: ellipse(60% 100% at 50% 100%);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 5vh;
  z-index: 2;
  box-shadow: 0 -10px 30px rgba(0, 0, 0, 0.1);
  animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { transform: translate(-50%, 100%); }
  to { transform: translate(-50%, 0); }
}

.login-content {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

/* Form Header */
.form-header {
  text-align: center;
  margin-bottom: 25px;
}

.form-header h1 {
  font-size: 38px;
  font-weight: 900;
  color: #000; /* Pure black text */
  margin: 0;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 14px;
  color: #000;
  margin-top: 5px;
}

/* Form */
.login-form {
  width: 100%;
}

.input-field {
  margin-bottom: 18px;
}

.input-group {
  width: 100%;
  height: 48px;
  background: #ffffff; /* Solid white background for inputs */
  border: 1px solid #ddd;
  border-radius: 24px;
  display: flex;
  align-items: center;
  padding-left: 20px;
  padding-right: 10px; /* Reduced right padding for "hard right" icon */
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.input-group:focus-within {
  border-color: #731111;
  box-shadow: 0 0 0 3px rgba(115, 17, 17, 0.1);
}

.input-group.has-error {
  border-color: #dc2626;
  background: #fef2f2;
}

.icon {
  color: #333; /* Darker icons */
  margin-right: 12px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.input-group input {
  border: none;
  outline: none;
  flex: 1;
  background: transparent;
  font-size: 15px;
  color: #000;
  padding: 0;
  min-width: 0; /* Ensures flex child can shrink if needed */
}

.password-toggle {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px; /* Fixed width to center icon */
  height: 32px;
  flex-shrink: 0;
}

.password-toggle:hover {
  color: #000;
}

.forgot {
  text-align: right;
  margin-top: -10px;
  margin-bottom: 30px;
  padding-right: 15px;
}

.forgot a {
  font-size: 11px;
  color: #000; /* Black font */
  text-decoration: underline; /* Underlined per the image */
}

.forgot a:hover {
  color: #731111;
}

.error-message {
  background: #fef2f2;
  color: #dc2626;
  font-size: 13px;
  padding: 12px 18px;
  border-radius: 26px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #fee2e2;
}

.submit-container {
  display: flex;
  justify-content: center;
}

.login-btn {
  width: 130px; /* Shorter pill button */
  height: 42px;
  background: #731111;
  color: #fff;
  border: none;
  border-radius: 21px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(115, 17, 17, 0.3);
}

.login-btn:hover:not(:disabled) {
  background: #8e1515;
  transform: translateY(-1px);
  box-shadow: 0 6px 15px rgba(115, 17, 17, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Accessibility */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

@media (max-width: 768px) {
  .login-arc-container {
    width: 250vw; /* Adjust for mobile curve */
    height: 70vh;
  }
}
</style>
