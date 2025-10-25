<template>
  <div class="app-shell">
    <div class="app-shell__background" aria-hidden="true"></div>
    <div class="app-shell__inner">
      <router-view />
      <NotificationHost />
    </div>
  </div>
</template>

<script setup>
import NotificationHost from './components/NotificationHost.vue';
</script>

