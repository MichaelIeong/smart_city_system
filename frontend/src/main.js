import Vue from 'vue'
import App from './App.vue'
import router from './router';
import axios from 'axios';
import Element from "element-ui";
import 'element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false;

axios.defaults.baseURL = 'http://localhost:8080'
Vue.prototype.$http = axios;

Vue.use(Element)


new Vue({
  router,
  render: h => h(App),
}).$mount('#app');