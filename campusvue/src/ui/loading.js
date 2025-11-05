import { ref } from 'vue';

const activeCount = ref(0);
const message = ref('');

export function showLoading(msg = '') {
  message.value = msg;
  activeCount.value += 1;
}

export function hideLoading() {
  if (activeCount.value > 0) activeCount.value -= 1;
  if (activeCount.value === 0) message.value = '';
}

export function useLoadingStore() {
  return { activeCount, message };
}

