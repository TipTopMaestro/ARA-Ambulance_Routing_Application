<template>
  <TopNav v-if="user" />
  <router-view />
</template>

<script setup>
import TopNav from './components/TopNav.vue'
import { useAuth } from './composables/useAuth'

const { user } = useAuth()
</script>
