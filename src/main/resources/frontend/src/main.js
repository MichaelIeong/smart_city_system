import Vue from 'vue'
import App from './App.vue'
import './plugins/element.js'
import axios from 'axios'
import VueAxios from 'vue-axios'
import router from './router';

// Vue.config.productionTip = false
// Vue.use(VueAxios, axios);
//
// new Vue({
//   render: function (h) { return h(App) },
// }).$mount('#app')

Vue.config.productionTip = false;

new Vue({
  router,
  render: h => h(App),
}).$mount('#app');