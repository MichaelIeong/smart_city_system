import request from '@/utils/request'
import store from '@/store'

const api = {
  project: '/api/import/upload',
  user: '/user',
  role: '/role',
  rule: '/api/fusion/getRuleList',
  permission: '/permission',
  permissionNoPager: '/permission/no-pager',
  orgTree: '/org/tree',
  tap: '/api/taps',
  tapExector: '/api/tapExecutor',
  events: '/api/events',
  spaces: '/api/spaces',
  properties: '/api/properties',
  services: '/api/services',
  deviceConfig: '/api/LHA',
  device: '/api/devices',
  fusionExecute: '/api/fusion/executeRule',
  fusionPause: '/api/fusion/pauseRule',
  fusionDelete: '/api/fusion/deleteRule',
  sensors: '/api/node-red/sensors',
  grid: '/api/grid',
  sceneAdd: '/api/scene/add',
  isResources: '/api/is_resources'
}

export default api

export function getUserList (parameter) {
  return request({
    url: api.user,
    method: 'get',
    params: parameter
  })
}

export function postProject (formData) {
  const token = store.state.token
  return request({
    url: api.project,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getRoleList (parameter) {
  return request({
    url: api.role,
    method: 'get',
    params: parameter
  })
}

export function getRuleList () {
  const token = store.state.token
  return request({
    url: api.rule,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getServiceList (projectId) {
  const token = store.state.token
  return request({
    url: api.services + '/getServiceListByProject',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    params: { projectId }
  })
}

export function addEnvService (data) {
  return request({
    url: '/api/env-services', // 这里对应你后端 EnvServiceInfoController 的 PostMapping
    method: 'post',
    data: data
  })
}
export function getPermissions (parameter) {
  return request({
    url: api.permissionNoPager,
    method: 'get',
    params: parameter
  })
}

export function getOrgTree (parameter) {
  return request({
    url: api.orgTree,
    method: 'get',
    params: parameter
  })
}

export function saveService (parameter) {
  return request({
    url: api.service,
    method: parameter.id === 0 ? 'post' : 'put',
    data: parameter
  })
}

export function saveSub (sub) {
  return request({
    url: '/sub',
    method: sub.id === 0 ? 'post' : 'put',
    data: sub
  })
}

export function getTapList (parameter) {
  const projectId = localStorage.getItem('project_id')
  return request({
    url: api.tap + `?project=${projectId}`,
    method: 'get',
    params: parameter
  })
}

export function getTapDetail (parameter) {
  const token = store.state.token
  return request({
    url: api.tap + `/${parameter.id}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    timeout: 60000
  })
}

export function saveTap (parameter) {
  return request({
    url: parameter.id === 0 ? api.tap : api.tap + `/${parameter.id}`,
    method: parameter.id === 0 ? 'post' : 'put',
    data: parameter
  })
}

export function deleteTap (parameter) {
  const token = store.state.token
  return request({
    url: api.tap + `/${parameter.id}`,
    method: 'delete',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function deleteTaps (ids) {
  return request({
    url: api.tap + `?id=${ids.join('&id=')}`,
    method: 'delete'
  })
}

export function getEvents (projectId) {
  const token = store.state.token
  return request({
    url: api.events + `?project=${projectId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getSpaces (projectId) {
  const token = store.state.token
  return request({
    url: api.spaces + `?project=${projectId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getProperties (projectId) {
  const token = store.state.token
  return request({
    url: api.properties + `?project=${projectId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getServices (projectId) {
  const token = store.state.token
  return request({
    url: api.services + `?project=${projectId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function saveDeviceConfig (deviceConfig) {
  const token = store.state.token
  return request({
    url: api.deviceConfig + '/addConfig',
    method: 'post',
    data: deviceConfig,
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getDeviceConfig (deviceId) {
  const token = store.state.token
  return request({
    url: api.deviceConfig + '/getConfig',
    method: 'get',
    params: { deviceId },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getDevicelha (deviceId) {
  const token = store.state.token
  return request({
    url: api.deviceConfig + '/getLHA',
    method: 'get',
    params: { deviceId },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function saveDeviceLha (deviceId, lha) {
  const token = store.state.token
  return request({
    url: api.deviceConfig + '/updateLHA',
    method: 'post',
    params: { deviceId },
    data: lha,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
}

export function getCSP (serviceId) {
  const token = store.state.token
  return request({
    url: api.services + '/getCSP',
    method: 'get',
    params: { serviceId },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function saveCsp (serviceId, csp) {
  const token = store.state.token
  return request({
    url: api.services + '/generateCSPbyHand',
    method: 'post',
    params: { serviceId },
    data: csp,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
}

// 新增：执行规则
export function executeRuleById (ruleId) {
  const token = store.state.token
  return request({
    url: `${api.fusionExecute}/${ruleId}`,
    method: 'post',
        headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 新增：暂停规则
export function pauseRuleById (ruleId) {
  const token = store.state.token
  return request({
    url: `${api.fusionPause}/${ruleId}`,
    method: 'put',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 新增：删除规则
export function deleteRuleById (ruleId) {
  const token = store.state.token
  return request({
    url: `${api.fusionDelete}/${ruleId}`,
    method: 'delete',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getSensors (projectId) {
  const token = store.state.token // 从 Vuex 或其他存储中获取 token

  return request({
    url: `${api.sensors}/${projectId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}` // 将 JWT token 添加到请求头
    }
  })
}

export function listTapRule ({ projectId, eventType, description, pageNo, pageSize, sortField, sortOrder }) {
  const token = store.state.token
  return request({
    url: `${api.tap}/list/${projectId}`,
    method: 'post',
    data: {
      eventType,
      description,
      pageNo,
      pageSize,
      sortField,
      sortOrder
    },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 生成自然语言描述 tap 规则
export function generateNaturalRule (uuid, message, gridId) {
  const token = store.state.token
  return request({
    url: `${api.tap}/recommend/generateNaturalRule`,
    method: 'post',
    data: {
      uuid,
      message,
      gridId
    },
    headers: {
      'Authorization': `Bearer ${token}`
    },
    timeout: 30000
  })
}

// 匹配已有的tap规则
export function findSimilarRules (message) {
  const token = store.state.token
  return request({
    url: `${api.tap}/recommend/findSimilarRule`,
    method: 'post',
    data: {
      message
    },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 生成 json 形式的 tap 规则
export function generateJsonRule (uuid, message, gridId) {
  const token = store.state.token
  return request({
    url: `${api.tap}/recommend/generateJsonRule`,
    method: 'post',
    data: {
      uuid,
      message,
      gridId
    },
    headers: {
      'Authorization': `Bearer ${token}`
    },
    timeout: 30000
  })
}

// 应用 json 转换
export function convertJsonRule (ruleJson) {
  const token = store.state.token
  return request({
    url: `${api.tap}/recommend/convertJsonRule`,
    method: 'post',
    data: {
      ruleJson
    },
    headers: {
      'Authorization': `Bearer ${token}`
    },
    timeout: 60000
  })
}

// 保存tap规则
export function createTapRule (projectId, description, ruleJson, flowJson, gridId, appName) {
  const token = store.state.token
  return request({
    url: `${api.tap}/create`,
    method: 'post',
    data: {
      projectId,
      description,
      ruleJson,
      flowJson,
      gridId,
      appName
    },
    headers: {
      'Authorization': `Bearer ${token}`
    },
    timeout: 120000
  })
}

// 更新tap规则
export function updateTapRule (id, description, flowJson) {
  const token = store.state.token
  return request({
    url: `${api.tap}/update`,
    method: 'post',
    data: {
      id,
      description,
      flowJson
    },
    headers: {
      'Authorization': `Bearer ${token}`
    },
    timeout: 120000
  })
}

// 修改应用状态
export function setExecuteTapEnabled (id, enabled) {
  const token = store.state.token
  return request({
    url: `${api.tap}/execute/enabled/${id}?enabled=${enabled}`,
    method: 'post',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取运行中的事件
export function getRunningEvents (id) {
  const token = store.state.token
  return request({
    url: `${api.tapExector}/getRunningEvents`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取运行中的事件实例
export function getWaitValueOfEvent (eventType) {
  const token = store.state.token
  return request({
    url: `${api.tapExector}/getWaitValueOfEvent?eventType=${eventType}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取日志
export function getLog (eventType, waitValue) {
  const token = store.state.token
  return request({
    url: `${api.tapExector}/getLog?eventType=${eventType}&waitValue=${waitValue}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 根据网格Id获取网格数据
export function getGridById (gridId) {
  const token = store.state.token
  return request({
    url: `${api.grid}/base/${gridId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取同类型的网格列表
export function getGridListByType (gridId) {
  const token = store.state.token
  return request({
    url: `${api.grid}/type/${gridId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 应用下发
export function syncAppRule (appId, gridIdList) {
  const token = store.state.token
  return request({
    url: `${api.tap}/sync`,
    method: 'post',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    data: {
      appId,
      gridIdList
    }
  })
}

// 查看应用执行详情
export function getAppExecuteDetail (appId) {
  const token = store.state.token
  return request({
    url: `${api.tap}/execute/detail/${appId}`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function addScene (sceneType) {
  const token = store.state.token

  return request({
    url: api.sceneAdd,
    method: 'post',
    data: {
      sceneType: sceneType
    },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getEnvInformationResources () {
  const token = store.state.token
  return request({
    url: api.isResources + '/information',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

export function getEnvSocialResources () {
  const token = store.state.token
  return request({
    url: api.isResources + '/social',
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}
