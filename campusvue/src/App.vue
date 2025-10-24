<template>
  <div class="app-shell">
    <router-view />
    <NotificationHost />
  </div>
</template>

<script setup>
import NotificationHost from './components/NotificationHost.vue';
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
}

.app-shell {
  min-height: 100%;
}
</style>
