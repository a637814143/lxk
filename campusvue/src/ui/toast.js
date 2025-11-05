import { reactive } from 'vue';

const store = reactive({ items: [] });
let idSeq = 1;

function push(type, message, duration = 3000) {
  const id = idSeq++;
  const item = { id, type, message };
  store.items.push(item);
  if (duration > 0) {
    setTimeout(() => remove(id), duration);
  }
  return id;
}

function remove(id) {
  const idx = store.items.findIndex(t => t.id === id);
  if (idx !== -1) store.items.splice(idx, 1);
}

export function useToast() {
  return {
    success: (msg, duration) => push('success', msg, duration),
    error: (msg, duration) => push('error', msg, duration),
    info: (msg, duration) => push('info', msg, duration)
  };
}

export function getToastStore() {
  return store;
}

