export const UNREAD_POLL_INTERVAL = Number(
  import.meta.env.VITE_UNREAD_POLL_INTERVAL_MS ?? 60000
);

export const shouldPoll = () => document.visibilityState === 'visible';

