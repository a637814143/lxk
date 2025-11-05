<template>
  <div v-if="store.activeCount > 0" class="overlay">
    <div class="spinner" />
    <p v-if="store.message" class="msg">{{ store.message }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useLoadingStore } from '../ui/loading';

const raw = useLoadingStore();
const store = {
  get activeCount() { return raw.activeCount.value; },
  get message() { return raw.message.value; }
};
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  z-index: 10000;
}
.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #93c5fd;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.msg { color: #fff; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>

