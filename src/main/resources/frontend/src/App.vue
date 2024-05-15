<template>
  <div id="app">
    <!-- <HelloWorld msg="Welcome to Your Vue.js App" /> -->
    <application></application>
  </div>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'
import application from './components/application.vue'

export default {
  name: 'app',
  components: {
    HelloWorld,
    application
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
</style>
