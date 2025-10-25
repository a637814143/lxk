<template>
  <div class="app-shell">
    <div class="app-shell__backdrop app-shell__backdrop--primary" aria-hidden="true"></div>
    <div class="app-shell__backdrop app-shell__backdrop--secondary" aria-hidden="true"></div>
    <div class="app-shell__content">
      <router-view />
      <NotificationHost />
    </div>
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
  position: relative;
  min-height: 100%;
  overflow: hidden;
  background: radial-gradient(circle at top, rgba(79, 70, 229, 0.14), transparent 55%),
    radial-gradient(circle at bottom, rgba(16, 185, 129, 0.12), transparent 45%),
    linear-gradient(180deg, #eef2ff 0%, #f8fafc 55%, #eef2ff 100%);
}

.app-shell__backdrop {
  position: fixed;
  width: 60vw;
  max-width: 640px;
  aspect-ratio: 1 / 1;
  filter: blur(120px);
  opacity: 0.55;
  pointer-events: none;
  z-index: 0;
  transform: translate3d(0, 0, 0);
}

.app-shell__backdrop--primary {
  top: -18vw;
  right: -12vw;
  background: #6366f1;
}

.app-shell__backdrop--secondary {
  bottom: -20vw;
  left: -14vw;
  background: #22d3ee;
  opacity: 0.45;
}

.app-shell__content {
  position: relative;
  min-height: 100vh;
  padding: clamp(16px, 4vw, 32px) clamp(12px, 6vw, 48px) 56px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  z-index: 1;
}

.app-shell__content > :first-child {
  flex: 1 0 auto;
}

@media (max-width: 720px) {
  .app-shell__content {
    padding: 16px 16px 48px;
  }
}
</style>
