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
  events: '/api/events',
  spaces: '/api/spaces',
  properties: '/api/properties',
  services: '/api/services',
  deviceConfig: '/api/LHA',
  fusionExecute: '/api/fusion/executeRule',
  fusionPause: '/api/fusion/pauseRule',
  fusionDelete: '/api/fusion/deleteRule',
  sensors: '/api/sensors/list'
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

export function getServiceList (project) {
  const token = store.state.token
  return request({
    url: api.services,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    params: { project }
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
  return request({
    url: api.tap + `?project=1`,
    method: 'get',
    params: parameter
  })
}

export function getTapDetail (parameter) {
  return request({
    url: api.tap + `/${parameter.id}`,
    method: 'get'
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
  return request({
    url: api.tap + `/${parameter.id}`,
    method: 'delete'
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
  return request({
    url: `${api.fusionExecute}/${ruleId}`,
    method: 'post'
  })
}

// 新增：暂停规则
export function pauseRuleById (ruleId) {
  return request({
    url: `${api.fusionPause}/${ruleId}`,
    method: 'put'
  })
}

// 新增：删除规则
export function deleteRuleById (ruleId) {
  return request({
    url: `${api.fusionDelete}/${ruleId}`,
    method: 'delete'
  })
}

// 新增：获取传感器列表（LLMCreation 可能用到）
export function getSensors () {
  const token = store.state.token
  return request({
    url: api.sensors,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}
