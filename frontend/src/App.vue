<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import '@/styles/global.css'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
}

body {
  margin: 0;
  padding: 0;
}
</style>