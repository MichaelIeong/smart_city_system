import Vue from 'vue'
import App from './App.vue'
import './plugins/element.js'
import router from './router';
import axios from 'axios';

Vue.config.productionTip = false;

axios.defaults.baseURL = 'http://localhost:8080'
Vue.prototype.$http = axios;

new Vue({
  router,
  render: h => h(App),
}).$mount('#app');