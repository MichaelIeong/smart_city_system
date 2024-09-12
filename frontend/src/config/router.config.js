// eslint-disable-next-line
import {UserLayout, BasicLayout} from '@/layouts'

const RouteView = {
  name: 'RouteView', render: h => h('router-view')
}

export const asyncRouterMap = [{
  path: '/',
  name: 'index',
  component: BasicLayout,
  meta: { title: 'menu.home' },
  redirect: '/project-selection',

  children: [
    // 项目选择
    {
      path: '/project-selection',
      name: 'projectSelection',
      hidden: true,
      component: () => import('@/views/user/ProjectSelection'),
      meta: { title: 'menu.projectSelection', keepAlive: false, permission: ['user'] }
    },

    // 资源管理
    {
      path: '/resource',
      name: 'resource',
      redirect: '/resource/physical-resource',
      component: RouteView,
      meta: {
        title: 'menu.resource',
        keepAlive: true,
        icon: 'database',
        hideChildrenInMenu: false,
        permission: ['dashboard']
      },
      children: [{
        // 物理资源
        path: '/resource/physical-resource/:pageNo([1-9]\\d*)?',
        name: 'PhysicalResource',
        // component: () => import('@/views/dashboard/Analysis'),
        meta: { title: 'menu.resource.physical-resource', keepAlive: false, permission: ['dashboard'] }
      }, // 信息资源
        {
          path: '/resource/cyber-resource',
          name: 'CyberResource',
          meta: { title: 'menu.resource.cyber-resource' }
        }, // 社会资源
        {
          path: '/resource/social-resource',
          name: 'SocialResource',
          // component: () => import('@/views/dashboard/Workplace'),
          meta: { title: 'menu.resource.social-resource', keepAlive: true, permission: ['dashboard'] }
        }]
    }, // 场景展示
    {
      path: '/space-scene',
      name: 'space-scene',
      component: () => import('@/views/list/TableList'),
      meta: { title: 'menu.space-scene', keepAlive: true, icon: 'picture', permission: ['dashboard'] }
    }, // 事件融合
    {
      path: '/event-fusion',
      name: 'event-fusion',
      component: () => import('@/views/list/EventFusionList'),
      meta: { title: 'menu.event-fusion', keepAlive: true, icon: 'fork', permission: ['table'] }
    },

    // 服务组合
    {
      path: '/service-group',
      name: 'service-group',
      component: RouteView,
      redirect: '/service-group/service-group-list',
      meta: { title: 'menu.service-group', icon: 'profile', permission: ['profile'] }
    },

    // result
    {
      path: '/result',
      name: 'result',
      component: RouteView,
      hidden: true,
      redirect: '/result/success',
      meta: { title: 'menu.result', icon: 'check-circle-o', permission: ['result'] },
      children: [{
        path: '/result/success',
        name: 'ResultSuccess',
        component: () => import(/* webpackChunkName: "result" */ '@/views/result/Success'),
        meta: {
          title: 'menu.result.success',
          keepAlive: false,
          hiddenHeaderContent: true,
          permission: ['result']
        }
      }, {
        path: '/result/fail',
        name: 'ResultFail',
        component: () => import(/* webpackChunkName: "result" */ '@/views/result/Error'),
        meta: { title: 'menu.result.fail', keepAlive: false, hiddenHeaderContent: true, permission: ['result'] }
      }]
    },

    // account
    {
      path: '/account',
      component: RouteView,
      redirect: '/account/center',
      name: 'account',
      hidden: true,
      meta: { title: 'menu.account', icon: 'user', keepAlive: true, permission: ['user'] },
      children: [
        {
          path: '/account/center',
          name: 'center',
          component: () => import('@/views/account/center'),
          meta: { title: 'menu.account.center', keepAlive: true, permission: ['user'] }
        },
        {
          path: '/account/settings',
          name: 'settings',
          component: () => import('@/views/account/settings/Index'),
          meta: { title: 'menu.account.settings', hideHeader: true, permission: ['user'] },
          redirect: '/account/settings/basic',
          hideChildrenInMenu: true,
          children: [
            {
              path: '/account/settings/basic',
              name: 'BasicSettings',
              component: () => import('@/views/account/settings/BasicSetting'),
              meta: { title: 'account.settings.menuMap.basic', hidden: true, permission: ['user'] }
            },
            {
              path: '/account/settings/security',
              name: 'SecuritySettings',
              component: () => import('@/views/account/settings/Security'),
              meta: {
                title: 'account.settings.menuMap.security',
                hidden: true,
                keepAlive: true,
                permission: ['user']
              }
            },
            {
              path: '/account/settings/binding',
              name: 'BindingSettings',
              component: () => import('@/views/account/settings/Binding'),
              meta: {
                title: 'account.settings.menuMap.binding',
                hidden: true,
                keepAlive: true,
                permission: ['user']
              }
            },
            {
              path: '/account/settings/notification',
              name: 'NotificationSettings',
              component: () => import('@/views/account/settings/Notification'),
              meta: {
                title: 'account.settings.menuMap.notification',
                hidden: true,
                keepAlive: true,
                permission: ['user']
              }
            }
          ]
        }
      ]
    },

    // Exception
    {
      path: '/exception',
      name: 'exception',
      component: RouteView,
      hidden: true,
      redirect: '/exception/403',
      children: [{
        path: '/exception/403',
        name: 'Exception403',
        component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/403'),
        meta: { title: 'menu.exception.not-permission', permission: ['exception'] }
      }, {
        path: '/exception/404',
        name: 'Exception404',
        component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404'),
        meta: { title: 'menu.exception.not-find', permission: ['exception'] }
      }, {
        path: '/exception/500',
        name: 'Exception500',
        component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/500'),
        meta: { title: 'menu.exception.server-error', permission: ['exception'] }
      }]
    }]
},
  {
    path: '*',
    redirect:
      '/404',
    hidden:
      true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      },
      {
        path: 'iotlogin',
        name: 'iotlogin',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/IoTLogin')
      },
      {
        path: 'register',
        name: 'register',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Register')
      },
      {
        path: 'register-result',
        name: 'registerResult',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/RegisterResult')
      },
      {
        path: 'recover',
        name: 'recover',
        component: undefined
      }
    ]
  },
  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]
