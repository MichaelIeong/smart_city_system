/**
 * @author cjzong
 * @desc MetaSpace.js
 * @date 2024/1/4
 */

import { createWebglEngine, webglPlugin } from '@tslfe/dt-engine'


class MetaSpace {
  /**
   * 实例化
   * @param $el - 挂在的DOM元素
   * @param _options - 配置参数
   */
  constructor($el, _options) {
    // if (!$el) {
    //   return console.warn('MetaSpace 初始化需要DOM节点')
    // }
    this.$el = $el
    // 配置
    this.options = Object.assign(
      {
        appCode: 'pad_listen',
        osUrl: 'ws://10.7.206.33:32206',
        mode: '2d',
        skyColor: '#F7F8FA',
        // 相机最大往外移动距离
        maxDistance: 1500,
        // 性能监控
        performance: null,
        // 0 - fps 监视帧数模式，默认模式；
        // 1 - ms 监视帧渲染时间模式；
        // 2 - mb 监视内存占用模式；
        // 3 - custom 常规模式
        // 能否点击的判断，会有高亮效果
        clickableFunc: null,
        // 点击回调
        clickFunc: null,
        // hover 高亮的颜色
        hoverColor: '#03E0FA 0.2',
        // selection 圈选的颜色
        selectColor: '#cb2e04',
        // path路径的颜色
        pathColor: '#cb2e04',
      },
      _options
    )
    // 插件
    this.plugins = {}
    // 事件
    this.events = {}
    // 组件的数据存储
    this.cmps = {}
  }
  // 应用初始化
  init() {
    return createWebglEngine((config) => {
      config.decoderPath = "./dt-engine/draco/"
      config.scene.cache = true;
      config.scene.cacheType = "enter";
      this.initDefaultPlugins(config.plugins)
      return config
    }).then(async (meta) => {
      if (this.$el) {
        const { skyColor, mode } = this.options
        this.$meta = meta
        meta.amount(this.$el)
        meta.scene.skycolor(skyColor)
        await this.initOSPlugin(meta)
        // this.initCtrlPlugin(meta)
        // this.initStatsPlugin(meta)
        // this.initPoiPlugin(meta)
        // this.initSelectionPlugin(meta)
        // this.initPathPlugin(meta)
        // this.initToolPlugin(meta)
        // this.initPickerPlugin(meta)
        // this.initEvents(meta)
        // this.changeMode(mode)
        window.meta = meta
      } else {
        this.$meta = meta;
        await this.initOSPlugin(meta);
      }

      return meta
    })
  }
  // 挂载元空间到dom上
  amountMetaOnDom() {
    const meta = this.$meta;
    const { skyColor } = this.options;
    meta.amount(this.$el);
    this.initPlugin(meta)
    meta.scene.skycolor(skyColor);
  }
  // 初始化所有的插件
  initPlugin(meta) {
    const { mode } = this.options
    this.initCtrlPlugin(meta);
    this.initStatsPlugin(meta);
    this.initPoiPlugin(meta);
    this.initSelectionPlugin(meta);
    this.initPathPlugin(meta);
    this.initToolPlugin(meta);
    this.initEvents(meta);
    this.changeMode(mode);
  }
  // 初始化查找父类
  initPickerPlugin({ plugin }) {
    if (webglPlugin.picker) {
      this.plugins.$picker = plugin.use(webglPlugin.picker())
    }

  }
  // 初始化停车场
  initPark(url) {
    url = 'http://10.7.206.33:9000/modelstudio/Output/wenyanan/科大讯飞_2/Park_e50d76e1b0bd4a91869076afc36e6a01/Building_b5ea8e64a0d74ab7a99962bd38ee9f5e/Floor_5a9d39115ca14278bb81c35d0d49cd99/graphic.glb'
    // 方式一
    let space = this.$meta.plugin.use(webglPlugin.space());
    let parking = this.$meta.get('1210267394518421504') //;
    this.$meta.loadOption(url).then((options) => {
      console.log('初始化参数--->', parking, url)
      space.render(parking, { url: url, replace: true }).then(target => {
        console.log("target--->", target)
        // target.model.position.set(0, 0, 0);
        // target.model.color = 'blue 0.5'
      })
    })

    // 方式二
    // let parking = app.get('停车场数字空间ID');
    // let url = "模型前缀" + parking?.model.url;
    // app.loadOption(url).then((options) => {
    //   app.createComponent(options).then((child) => {
    //     parking.replace(child);
    //     child.model.position.set(0, 0, 0);
    //   });
    // });
  }
  // 初始化灯光插件
  initDefaultPlugins(plugins) {
    plugins[1] = webglPlugin.light([
      { type: 'AmbientLight', color: '#d8dae2', intensity: 5 },
      { type: 'DirectionalLight', color: '#d8dae2', intensity: 5 },
    ])
  }

  // 初始化地面
  initGround(size) {
    const { scene } = this.$meta
    if (this.groundInfo) {
      scene.remove(this.groundInfo.ground)
      scene.remove(this.groundInfo.grid)
    }
    // 模型比较小的时候，加个底
    if (size < 200) {
      this.groundInfo = createGround(size)
      scene.add(this.groundInfo.ground)
      scene.add(this.groundInfo.grid)
    }
  }

  // 初始化TacOS数字空间交互插件
  initOSPlugin({ plugin }) {
    const { appCode, osUrl } = this.options
    if (!appCode || !osUrl) {
      return
    }
    this.plugins.$os = plugin.use(webglPlugin.os())
    return this.plugins.$os.connect({
      appCode,
      socket: { url: osUrl },
    })
  }

  // 初始化控制器插件
  initCtrlPlugin({ plugin }) {
    // 修改控制器操作方式
    let $orbit = plugin.use(webglPlugin.orbitControl());
    $orbit.control.mouseButtons = {
      LEFT: 2,
      MIDDLE: 1,
      RIGHT: 0,
    }
    $orbit.control.maxDistance = this.options.maxDistance
    this.plugins.$orbit = $orbit
  }

  // 初始化性能监控插件
  initStatsPlugin({ plugin }) {
    const { performance: mode } = this.options
    if (![0, 1, 2, 3].includes(mode)) {
      return
    }
    this.plugins.$stats = plugin.use(webglPlugin.stats({ mode }))
  }

  // 初始化POI 打点插件
  initPoiPlugin({ plugin }) {
    this.plugins.$poi = plugin.use(webglPlugin.poi2d())
  }

  // 初始化boxSelection 圈选组件 插件
  initSelectionPlugin({ plugin }) {
    const { selectColor } = this.options
    this.plugins.$selection = plugin.use(
      webglPlugin.boxSelection({
        color: selectColor || 'red',
        mode: 'rect',
      })
    )
    this.plugins.$selection.addEventListener('end', this.selectionEvtListener.bind(this))
  }
  selectionEvtListener() {
    this.plugins.$orbit.enabled = true
    this.plugins.$orbit.mode(this.options.mode)
    if (this._selectionResolve) {
      this._selectionResolve(this.plugins.$selection.list.slice(0))
      delete this._selectionResolve
    }
    this.plugins.$selection.clear()
  }
  // 初始化 path 路劲 插件
  initPathPlugin({ plugin }) {
    this.plugins.$path = plugin.use(webglPlugin.path())
  }
  // 初始化 tool 工具 插件
  initToolPlugin({ plugin }) {
    this.plugins.$tool = plugin.use(webglPlugin.tool())
    // this.plugins.$tool.convertor =
    //   this.plugins.$tool.buildCoordinateConvertor(GPS_POINTS)
  }

  // 初始化事件监听
  initEvents(meta) {
    this.events.hover = []
    this.events.click = []

    const { hoverColor, clickableFunc, clickFunc } = this.options
    meta.addEventListener('hover', this.hoverEvtListener.bind(this))
    meta.addEventListener('click', this.clickEvtListener.bind(this))
  }
  hoverEvtListener(evt) {
    let lastCmp = null
    const { hoverColor, clickableFunc } = this.options
    this.events.hover.forEach((handler) => handler(evt))
    if (!clickableFunc) {
      return
    }
    const curCmp = clickableFunc(evt) ? evt.params.component : null
    if (lastCmp === curCmp) {
      return
    }
    if (lastCmp) {
      lastCmp.model.color = lastCmp.model._currentColor
    }
    if (curCmp) {
      curCmp.model.color = hoverColor
    }
    lastCmp = curCmp
    this.$el.style.cursor = curCmp ? 'pointer' : 'default'
  }
  clickEvtListener(evt) {
    const { clickableFunc, clickFunc } = this.options
    this.events.click.forEach((handler) => handler(evt))
    if (clickableFunc && clickFunc) {
      const curCmp = clickableFunc(evt) ? evt.params.component : null
      clickFunc(curCmp)
    }
  }
  /**
   * 根据模型Path模型
   * @param modelPath  模型路径
   * @returns {*|void}
   */
  enterByPath(modelPath) {
    const { $meta } = this
    const { $path } = this.plugins
    if (!$meta || !$path) {
      console.warn('MetaSpace 未初始化')
      return Promise.resolve(null)
    }
    if (!modelPath) {
      console.warn('缺少模型地址')
      return Promise.resolve(null)
    }
    $meta.clear()
    return this.plugins.$os
      .enter(modelPath, (url) =>
        url.replace(/^.*?\/modelstudio\/Output\//, '/modelstudio/Output/')
      )
      .then((cmp) => {
        //this.initPark(tempUrl)
        if (!cmp?.model) {
          return null
        }
        const modelY = cmp.model.position.y || 0
        // 重新设置模型颜色
        cmp.model.color = ""
        // 地面高度设置为0
        cmp.model.position.y = 0
        // 根据当前模型构造适合的地面
        // const size = getModelSize(cmp)
        // this.currentModelSize = Math.max(size.x, size.z)
        // if (size) {
        //   const maxLen = Math.max(size.x, size.z)
        //   this.initGround(maxLen)
        // }
        // 生成照明回路
        // const lines = $path.defaultPathData
        // this.lightLines = (lines || []).map((line) =>
        //   $path.create(
        //     line.data.map((d) => ({ ...d, y: d.y - modelY })),
        //     {
        //       color: '#b8cafc',
        //       speed: 0,
        //       radius: 0.02,
        //       texture: null,
        //     }
        //   )
        // )
        // this.focusTo()
        return cmp
      })
  }

  /**
   * 视角聚焦
   */
  focusTo() {
    const { $meta } = this
    const { $orbit } = this.plugins
    if (!$meta || !$orbit) {
      return console.warn('MetaSpace 未初始化')
    }
    // 是否3D场景
    const is3d = this.options.mode === '3d'
    // 初始距离 为模型 尺寸的5倍
    // const distance = this.currentModelSize * 5
    const distance = 1;
    $meta.camera.adjust({
      distance,
      duration: 3000,
      theta: 180,
      phi: is3d ? 45 : .1,
      route: is3d ? null : 'spherical',
    })
  }

  // 添加事件监听
  $on(eventName, handler) {
    const { $meta, events } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    if (!events[eventName]) {
      events[eventName] = []
      $meta.addEventListener(eventName, (...args) => {
        events[eventName].forEach((handler) => handler(...args))
      })
    }
    events[eventName].push(handler)
  }

  // 注销事件
  $off(eventName, handler) {
    const { $meta, events } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    if (!events[eventName]) {
      return console.log('注销的事件未注册', eventName)
    }
    if (handler) {
      const index = events[eventName].indexOf(handler)
      if (index > -1) {
        events[eventName].splice(index, 1)
      } else {
        console.log('注销的事件未注册', eventName)
      }
    } else {
      events[eventName] = []
    }
  }

  // 派发事件
  $emit(eventName, ...args) {
    const { events } = this
    if (events[eventName]) {
      events[eventName].forEach((handler) => handler(...args))
    }
  }

  /**
   * 修改中点位置
   * @param modelId
   * @param position
   * @param gps
   */
  center({ modelId, position, gps }) {
    const { $meta } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    if (position) {
      $meta.camera.lookAt(position)
    } else if (gps) {
      const point = this.gps2point(gps)
      point && $meta.camera.lookAt(point)
    } else if (modelId) {
      const cmp = $meta.get(modelId)
      cmp && $meta.camera.lookAt(cmp.model.position)
    }
  }
  // 设置相机最大移动距离
  setMaxDistance({ plugin }, distance) {
    let $orbit = plugin.use(webglPlugin.orbitControl());
    $orbit.control.maxDistance = distance
    this.plugins.$orbit = $orbit
  }
  /**
   * 设置缩放等级
   * @param value 缩放层级
   */
  zoom(value) {
    const { $meta } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    const { camera } = $meta
    const zoom = Math.max(value, 1)
    camera.setZoom({ zoom, duration: 1000 })
  }

  /**
   * 放大
   */
  zoomIn() {
    const { camera } = this.$meta
    this.zoom(camera.zoom + 1)
  }

  /**
   * 缩小
   */
  zoomOut() {
    const { camera } = this.$meta
    this.zoom(camera.zoom - 1)
  }

  /**
   * 切换模式
   * @param mode "2d" | "3d"
   */
  changeMode(mode) {
    const { $meta } = this
    const { $orbit } = this.plugins
    if (!$meta || !$orbit) {
      return console.warn('MetaSpace 未初始化')
    }
    this.options.mode = mode
    $orbit.mode(mode)
    const is3d = mode === '3d'
    $orbit.control.maxPolarAngle = is3d ? Math.PI / 2 : 0

    const $light = $meta.plugin.get('light')
    $light.lights.forEach((light) => {
      light.intensity = is3d ? 5 : 3
    })
    this.focusTo()
  }

  /**
   * 根据ID 获取模型集合
   * @param ids
   */
  getCmpsByIds(ids) {
    const { $meta } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    return $meta.filter((cmp) => ids.includes(cmp.id))
  }

  /**
   * 根据类型 获取模型集合
   * @param type
   */
  getCmpsByType(type) {
    const { $meta } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    return $meta.search(type)
  }

  /**
   * 获取所有组件
   * @returns {*|*[]}
   */
  getAllCmps() {
    const { $meta } = this
    if (!$meta) {
      console.warn('MetaSpace 未初始化')
      return []
    }
    return $meta.filter(() => true)
  }

  /**
   * 根据id设置颜色
   * @param ids
   * @param color
   */
  setColorByIds(ids, color) {
    const { $meta } = this
    if (!$meta) {
      return console.warn('MetaSpace 未初始化')
    }
    console.log(JSON.stringify(ids));
    ids.forEach((id) => {
      const cmp = $meta.get(id, false)
      if (cmp) {
        cmp.model._currentColor = color
        cmp.model.color = color
        if (cmp.$marker && cmp.$marker.$el) {
          if (color) {
            cmp.$marker?.$el?.classList.add('is-active')
          } else {
            cmp.$marker?.$el?.classList.remove('is-active')
          }
        }
      }
    })
  }

  /**
   * 增加打点markers
   * @param data
   * @returns {Promise<*[]>}
   */
  async addMarkers(data = []) {
    const { $meta } = this
    const { $poi } = this.plugins
    const res = []
    if (!$meta || !$poi) {
      console.warn('MetaSpace 未初始化')
      return res
    }
    for (let item of data) {
      const marker = createMarker($poi, item)
      if (item.gps) {
        item.position = this.gps2point(item.gps)
      }
      if (marker) {
        const ids = await appendMarker($meta, $poi, marker)
        res.push(...ids)
      }
    }
    return res
  }

  /**
   * 移除制定的标点
   * @param list - 标点信息
   * @returns {*}
   */
  removeMarkers(list = []) {
    const { $meta } = this
    const { $poi } = this.plugins
    if (!$meta || !$poi) {
      return console.warn('MetaSpace 未初始化'), []
    }
    list.forEach((cmp) => delete cmp.$marker)
    return $poi.clear(list)
  }
  /***
   * 根据id删除设备摄像头图标
   */
  removeMakerById(list = []) {
    const { $meta } = this
    const { $poi } = this.plugins
    if (!$meta || !$poi) {
      return console.warn('MetaSpace 未初始化'), []
    }
    list.forEach((modelId) => {
      const cmp = $meta.get(modelId)
      $poi.clear(cmp)
    })
  }
  /**
   * 清除所有标点
   * @returns {*}
   */
  clearMarkers() {
    const { $meta } = this
    const { $poi } = this.plugins
    if (!$meta || !$poi) {
      return console.warn('MetaSpace 未初始化'), []
    }
    $meta.filter((cmp) => delete cmp.$marker)
    return $poi.clearAll()
  }

  /**
   * 添加路径
   * @param lines
   * @param options
   * @returns {*}
   */
  addPaths(lines, options) {
    const { $meta } = this
    const { $path } = this.plugins
    const { pathColor } = this.options
    if (!$meta || !$path) {
      return console.warn('MetaSpace 未初始化'), []
    }
    const paths = lines
      .map((item) => ({
        points: (Array.isArray(item) ? item : item.points || [])
          .map((d) => {
            if (d.gps) {
              return this.gps2point(d.gps)
            } else if (d.modelId) {
              const cmp = $meta.get(d.modelId)
              return cmp?.model?.position
            } else {
              return d.position
            }
          })
          .filter((d) => d)
          .reverse(),
        options: item.options || options || {},
      }))
      .filter((d) => d.points.length > 1)
    return paths.map((d) =>
      $path.create(d.points, {
        color: pathColor,
        radius: 2,
        indent: 0.1,
        radialSegments: 60,
        texture: require('./images/arrow.jpg'),
        ...d.options,
      })
    )
  }

  /**
   * 移除路径
   * @param paths
   */
  removePaths(paths) {
    const { $meta } = this
    const { $path } = this.plugins
    if (!$meta || !$path) {
      return console.warn('MetaSpace 未初始化'), []
    }
    paths.forEach((path) => $path.remove(path))
  }

  /**
   * 清空路径
   * @returns {*}
   */
  clearPaths() {
    const { $meta } = this
    const { $path } = this.plugins
    if (!$meta || !$path) {
      return console.warn('MetaSpace 未初始化'), []
    }
    return $path.clear()
  }

  /**
   * 根据类型是否展示模型
   * @param type 组件类型
   * @param visible 是否展示
   */
  visibleByType(type, visible) {
    const { $meta } = this
    const { $poi } = this.plugins
    if (!$meta || !$poi) {
      return console.warn('MetaSpace 未初始化')
    }
    const rootCmpId = $meta.component.id
    let group = []
    if (!visible) {
      if (type.startsWith('not:')) {
        // 排除的类型
        const kind = type.split(':')[1]
        const ts = type.split(':')[2]?.split(',') || []
        group = $meta.filter(
          (cmp) => cmp.ext.type === kind && !ts.includes(cmp.type)
        )
      } else {
        group = $meta.search(type).filter((d) => d.id !== rootCmpId)
      }
      this.cmps[type] = group
    } else {
      group = this.cmps[type] || []
      this.cmps[type] = []
    }
    if (type === 'p_switch_100') {
      this.lightLines.forEach((model) => (model.visible = visible))
    }
    group.forEach((cmp) => {
      if (visible) {
        $meta.add(cmp)
        if (cmp.$marker) {
          if (Array.isArray(cmp.$marker)) {
            cmp.$marker.forEach((m) => {
              markerApplyToCmp(m, m.parent, m.data.offset)
            })
          } else {
            markerApplyToCmp(cmp.$marker, cmp, cmp.$marker.data.offset)
          }
        }
      } else {
        cmp.removeFromParent()
        const ids = []
        if (cmp.$marker) {
          if (Array.isArray(cmp.$marker)) {
            ids.push(...cmp.$marker.map((d) => d.parent))
          } else {
            ids.push(cmp.$marker.parent)
          }
        }
        ids.length && $poi.clear(ids)
      }
    })
  }

  // 圈选操作
  selection() {
    const { $meta } = this
    const { $selection, $orbit } = this.plugins
    if (!$meta || !$selection || !$orbit) {
      return console.warn('MetaSpace 未初始化')
    }
    $orbit.mode('2d')
    $orbit.enabled = false
    return new Promise((resolve) => {
      $selection.clear()
      $selection.start()
      this._selectionResolve = resolve
    })
  }

  // 停止圈选
  stopSelection() {
    const { $meta } = this
    const { $selection, $orbit } = this.plugins
    if (!$meta || !$selection || !$orbit) {
      return console.warn('MetaSpace 未初始化')
    }
    $orbit.mode(this.options.mode)
    $orbit.enabled = true
    $selection.end()
    $selection.clear()
    delete this._selectionResolve
  }

  /**
   * gps 转 点位坐标
   * @param gps
   * @returns {*|void}
   */
  gps2point(gps) {
    const { $meta } = this
    const { $tool } = this.plugins
    if (!$meta || !$tool) {
      return console.warn('MetaSpace 未初始化')
    }
    const { longitude, latitude } = gps || {}
    if (!longitude || !latitude) return null
    const point = $tool.convertor.toVector3({
      longitude: +longitude.toFixed(6),
      latitude: +latitude.toFixed(6),
    })
    point.y = 0.5 // 固定高度
    return point
  }

  /**
   * 点位转GPS
   * @param point
   */
  point2gps(point) {
    const { $meta } = this
    const { $tool } = this.plugins
    if (!$meta || !$tool) {
      return console.warn('MetaSpace 未初始化')
    }
    // 固定高度
    return point ? $tool.convertor.toGps(point) : null
  }

  // 销毁
  destroy() {
    const { $meta } = this
    $meta.removeEventListener('hover', this.hoverEvtListener)
    $meta.removeEventListener('click', this.clickEvtListener)
    $meta.clear()
  }
}

export default MetaSpace
