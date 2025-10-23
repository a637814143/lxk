const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';

function resolveUrl(path) {
  if (!path.startsWith('/')) {
    return `${API_BASE}/${path}`;
  }
  return `${API_BASE}${path}`;
}

async function request(path, options = {}) {
  const url = resolveUrl(path);
  const finalOptions = {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    },
    ...options
  };

  if (finalOptions.body && typeof finalOptions.body !== 'string') {
    finalOptions.body = JSON.stringify(finalOptions.body);
  }

  const response = await fetch(url, finalOptions);
  if (response.status === 204) {
    return null;
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const message = data?.message ?? data?.error ?? '请求失败';
    throw new Error(message);
  }
  return data;
}

export function get(path) {
  return request(path, { method: 'GET' });
}

export function post(path, body) {
  return request(path, { method: 'POST', body });
}

export function put(path, body) {
  return request(path, { method: 'PUT', body });
}

export function del(path) {
  return request(path, { method: 'DELETE' });
}
