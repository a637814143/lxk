<template>
  <div class="toast-wrap">
    <div v-for="t in store.items" :key="t.id" class="toast" :class="t.type" @click="dismiss(t.id)">
      {{ t.message }}
    </div>
  </div>
</template>

<script setup>
import { getToastStore } from '../ui/toast';

const store = getToastStore();

function dismiss(id) {
  const idx = store.items.findIndex(t => t.id === id);
  if (idx !== -1) store.items.splice(idx, 1);
}
</script>

<style scoped>
.toast-wrap {
  position: fixed;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 9999;
}
.toast {
  padding: 10px 14px;
  border-radius: 10px;
  color: #0f172a;
  background: #eef2ff;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.15);
  cursor: pointer;
}
.toast.success { background: #dcfce7; color: #065f46; }
.toast.error { background: #fee2e2; color: #991b1b; }
.toast.info { background: #e0f2fe; color: #0c4a6e; }
</style>

