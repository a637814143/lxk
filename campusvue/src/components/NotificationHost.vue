<template>
  <div class="toast-container" aria-live="polite" aria-atomic="true">
    <transition-group name="toast" tag="div">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="toast"
        :class="`toast--${item.type}`"
        role="alert"
      >
        <span>{{ item.message }}</span>
        <button type="button" @click="remove(item.id)" aria-label="关闭提示">×</button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { useNotificationStore } from '../composables/useNotifier';

const { notifications, remove } = useNotificationStore();
</script>

<style scoped>
.toast-container {
  position: fixed;
  inset: 16px 16px auto auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
  pointer-events: none;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.98);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.toast {
  pointer-events: auto;
  min-width: 220px;
  max-width: clamp(220px, 28vw, 320px);
  padding: 12px 16px;
  border-radius: 14px;
  box-shadow: 0 14px 35px rgba(15, 23, 42, 0.18);
  background: #f1f5f9;
  color: #0f172a;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.toast button {
  border: none;
  background: transparent;
  color: inherit;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  line-height: 1;
}

.toast--success {
  background: #dcfce7;
  color: #166534;
}

.toast--error {
  background: #fee2e2;
  color: #b91c1c;
}

.toast--info {
  background: #dbeafe;
  color: #1d4ed8;
}
</style>
