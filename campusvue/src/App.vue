<template>
  <router-view />
  <GlobalToast />
  <GlobalLoading />
</template>

<script setup>
import GlobalToast from './components/GlobalToast.vue';
import GlobalLoading from './components/GlobalLoading.vue';
</script>

<style>
html,
body,
#app {
  height: 100%;
}
</style>

