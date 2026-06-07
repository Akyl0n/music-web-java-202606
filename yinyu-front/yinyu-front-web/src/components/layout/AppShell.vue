<script setup>
import TopHeader from './TopHeader.vue'
import PlayerBar from './PlayerBar.vue'
</script>

<template>
  <div class="app-shell">
    <TopHeader />
    <main class="page-shell">
      <router-view />
    </main>
    <PlayerBar />
  </div>
</template>
