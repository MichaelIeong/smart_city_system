// src/router/index.js
import Vue from 'vue';
import Router from 'vue-router';
import Home from '@/components/Home.vue';
import application from '@/components/application.vue';
import DeviceAccess from '@/components/DeviceAccess.vue';
import SpaceInitialization from "@/components/SpaceInitialization.vue";

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            name: 'Home',
            component: Home,
        },
        {
            path: '/application',
            name: 'application',
            component: application,
        },
        {
            path: '/device-access',
            name: 'DeviceAccess',
            component: DeviceAccess,
        },
        {
            path: '/space-init',
            name: 'SpaceInitialization',
            component: SpaceInitialization,
        },
    ],
});
