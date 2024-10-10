<template>
  <pro-layout
    :menus="menus"
    :collapsed="collapsed"
    :mediaQuery="query"
    :isMobile="isMobile"
    :handleMediaQuery="handleMediaQuery"
    :handleCollapse="handleCollapse"
    :i18nRender="i18nRender"
    v-bind="settings"
  >

    <template v-slot:menuHeaderRender>
      <div>
        <img src="@/assets/Fudan_University_Logo.svg" alt="icon" />
        <h1>{{ title }}</h1>
      </div>
    </template>

    <!-- 自定义 Header 左侧内容 -->
    <template v-slot:headerContentRender>
      <div>
        <a-button type="primary" @click="navigateToProjectSelection" style="margin-left: 24px;">
          项目选择
        </a-button>
      </div>
    </template>

    <template v-slot:rightContentRender>
      <right-content :top-menu="settings.layout === 'topmenu'" :is-mobile="isMobile" :theme="settings.theme" />
    </template>

    <template v-slot:footerRender>
      <global-footer />
    </template>
    <router-view />
  </pro-layout>
</template>

<script>
import { asyncRouterMap } from '@/config/router.config.js'
import { SettingDrawer, updateTheme } from '@ant-design-vue/pro-layout'
import { i18nRender } from '@/locales'
import { mapState } from 'vuex'
import { CONTENT_WIDTH_TYPE } from '@/store/mutation-types'

import defaultSettings from '@/config/defaultSettings'
import RightContent from '@/components/GlobalHeader/RightContent'
import GlobalFooter from '@/components/GlobalFooter'
import Ads from '@/components/Other/CarbonAds'

export default {
  name: 'BasicLayout',
  components: {
    SettingDrawer,
    RightContent,
    GlobalFooter,
    Ads
  },
  data () {
    return {
      isProPreviewSite: process.env.VUE_APP_PREVIEW === 'true' && process.env.NODE_ENV !== 'development',
      isDev: process.env.NODE_ENV === 'development' || process.env.VUE_APP_PREVIEW === 'true',
      menus: [],
      collapsed: false,
      title: defaultSettings.title,
      settings: {
        layout: defaultSettings.layout,
        contentWidth: defaultSettings.layout === 'sidemenu' ? CONTENT_WIDTH_TYPE.Fluid : defaultSettings.contentWidth,
        theme: defaultSettings.navTheme,
        primaryColor: defaultSettings.primaryColor,
        fixedHeader: defaultSettings.fixedHeader,
        fixSiderbar: defaultSettings.fixSiderbar,
        colorWeak: defaultSettings.colorWeak,
        hideHintAlert: false,
        hideCopyButton: false
      },
      query: {},
      isMobile: false
    }
  },
  computed: {
    ...mapState({
      mainMenu: state => state.permission.addRouters
    })
  },
  created () {
    const routes = asyncRouterMap.find((item) => item.path === '/')
    this.menus = (routes && routes.children) || []
  },
  mounted () {
    const userAgent = navigator.userAgent
    if (userAgent.indexOf('Edge') > -1) {
      this.$nextTick(() => {
        this.collapsed = !this.collapsed
        setTimeout(() => {
          this.collapsed = !this.collapsed
        }, 16)
      })
    }

    if (process.env.NODE_ENV !== 'production' || process.env.VUE_APP_PREVIEW === 'true') {
      updateTheme(this.settings.primaryColor)
    }
  },
  methods: {
    i18nRender,
    handleMediaQuery (val) {
      this.query = val
      if (this.isMobile && !val['screen-xs']) {
        this.isMobile = false
        return
      }
      if (!this.isMobile && val['screen-xs']) {
        this.isMobile = true
        this.collapsed = false
        this.settings.contentWidth = CONTENT_WIDTH_TYPE.Fluid
      }
    },
    handleCollapse (val) {
      this.collapsed = val
    },
    navigateToProjectSelection () {
      this.$router.push({ path: '/user/project-selection' })
    }
  }
}
</script>

<style lang="less">
@import "./BasicLayout.less";
</style>
