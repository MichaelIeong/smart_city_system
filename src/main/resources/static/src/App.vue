<!--<template>-->
<!--  <div id="app">-->
<!--    &lt;!&ndash; <HelloWorld msg="Welcome to Your Vue.js App" /> &ndash;&gt;-->
<!--    <application></application>-->
<!--  </div>-->
<!--</template>-->

<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<!--<script>-->
<!--import HelloWorld from './components/HelloWorld.vue'-->
<!--import application from './components/application.vue'-->

<!--export default {-->
<!--  name: 'app',-->
<!--  components: {-->
<!--    HelloWorld,-->
<!--    application-->
<!--  }-->
<!--}-->
<!--</script>-->

<script>
export default {
  name: 'App',
};
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
