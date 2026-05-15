import { ref, readonly } from 'vue'

const user = ref(JSON.parse(localStorage.getItem('ara_user') || 'null'))

export function useAuth() {
  const login = (userData) => {
    user.value = userData
    localStorage.setItem('ara_user', JSON.stringify(userData))
  }

  const logout = () => {
    user.value = null
    localStorage.removeItem('ara_user')
    window.location.href = '/login'
  }

  return {
    user: readonly(user),
    login,
    logout
  }
}
