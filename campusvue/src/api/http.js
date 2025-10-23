const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';
const AUTH_STORAGE_KEY = 'tsukiAuth';

let authInfo = loadAuthInfo();

function loadAuthInfo() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    console.warn('无法读取本地登录信息', error);
    return null;
  }
}

export function getAuthInfo() {
  return authInfo;
}

export function setAuthInfo(info) {
  authInfo = info;
  if (info) {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(info));
  } else {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }
}

export function clearAuthInfo() {
  setAuthInfo(null);
}

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

  if (authInfo?.token) {
    finalOptions.headers = {
      ...finalOptions.headers,
      Authorization: `Bearer ${authInfo.token}`
    };
  }

  const response = await fetch(url, finalOptions);
  if (response.status === 204) {
    return null;
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401) {
      clearAuthInfo();
    }
    const message = data?.message ?? data?.error ?? '请求失败';
    const error = new Error(message);
    error.status = response.status;
    throw error;
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

export function patch(path, body) {
  return request(path, { method: 'PATCH', body });
}
