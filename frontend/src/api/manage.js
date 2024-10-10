import request from '@/utils/request'
import store from '@/store'

const api = {
  project: 'api/import/upload',
  user: '/user',
  role: '/role',
  rule: '/api/fusion/getRuleList',
  service: '/service/getServiceList',
  permission: '/permission',
  permissionNoPager: '/permission/no-pager',
  orgTree: '/org/tree',
  tap: '/taps'
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
  return request({
    url: api.project, // 确保 api.project 指向正确的后端 /upload 路径
    method: 'post',
    data: formData, // 使用 data 而不是 params 来发送 FormData
    headers: {
      'Content-Type': 'multipart/form-data' // 设置 multipart/form-data 头
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
  const token = store.state.token // 从 Vuex 或其他存储中获取 token

  return request({
    url: api.rule,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}` // 将 JWT token 添加到请求头
    }
  })
}

export function getServiceList () {
  const token = store.state.token // 从 Vuex 或其他存储中获取 token

  return request({
    url: api.service,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}` // 将 JWT token 添加到请求头
    }
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

// id == 0 add     post
// id != 0 update  put
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
    url: api.tap,
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
    url: parameter.id === '0' ? api.tap : api.tap + `/${parameter.id}`,
    method: parameter.id === '0' ? 'post' : 'put',
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
