import { reactive } from 'vue';

const notifications = reactive([]);
let seed = 0;

function removeNotification(id) {
  const index = notifications.findIndex(item => item.id === id);
  if (index >= 0) {
    notifications.splice(index, 1);
  }
}

function pushNotification(type, message) {
  const id = ++seed;
  notifications.push({ id, type, message });
  setTimeout(() => removeNotification(id), 3200);
  return id;
}

export function notifySuccess(message) {
  return pushNotification('success', message);
}

export function notifyError(message) {
  return pushNotification('error', message);
}

export function notifyInfo(message) {
  return pushNotification('info', message);
}

export function useNotificationStore() {
  return {
    notifications,
    remove: removeNotification
  };
}
