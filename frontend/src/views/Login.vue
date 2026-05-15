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
          <!-- <h3>Ambulance Routing</h3>
          <p>Application</p> -->
        </div>
      </div>

      <!-- Right Side -->
      <div class="right-panel">
        <h1>WELCOME</h1>
        <p class="subtitle">Log in to your account to continue</p>

        <form class="login-form">
          <div class="input-group">
            <span class="icon">
              <i class="fas fa-user"></i>
            </span>
            <input type="text" placeholder="username" />
          </div>

          <div class="input-group">
            <span class="icon">
              <i class="fas fa-lock"></i>
            </span>
            <input type="password" placeholder="password" />
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

<script>
export default {
  name: "LoginPage",
};
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

.icon i {
  color: #8c8c8c;
  font-size: 13px;
}

.input-group input {
  border: none;
  outline: none;
  width: 100%;
  background: transparent;
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
