<template>
  <div class="app-shell">
    <div class="app-shell__background" aria-hidden="true"></div>
    <header class="app-topbar">
      <div class="app-topbar__inner">
        <router-link to="/" class="app-logo">Tsuki Campus</router-link>
        <nav class="app-topbar__nav" aria-label="全站导航">
          <router-link to="/login">管理员登录</router-link>
          <router-link to="/register">用户注册</router-link>
        </nav>
      </div>
    </header>
    <main class="app-shell__inner">
      <router-view />
      <NotificationHost />
    </main>
  </div>
</template>

<script setup>
import NotificationHost from './components/NotificationHost.vue';
</script>

