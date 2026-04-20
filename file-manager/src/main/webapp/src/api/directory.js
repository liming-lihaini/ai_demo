/**
 * 目录管理 API
 */

const baseUrl = '/api/directory'

/**
 * 获取根目录
 */
export function getRootDirectories(userId = 1) {
  return fetch(`${baseUrl}/root?userId=${userId}`).then(res => res.json())
}

/**
 * 获取子目录列表
 */
export function getChildDirectories(parentId) {
  return fetch(`${baseUrl}/children/${parentId}`).then(res => res.json())
}

/**
 * 创建目录
 */
export function createDirectory(name, parentId = null, userId = 1) {
  return fetch(`${baseUrl}/create?userId=${userId}&parentId=${parentId || ''}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, parentId })
  }).then(res => res.json())
}

/**
 * 重命名目录
 */
export function renameDirectory(id, name) {
  return fetch(`${baseUrl}/update`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, name })
  }).then(res => res.json())
}

/**
 * 移动目录
 */
export function moveDirectory(id, parentId) {
  return fetch(`${baseUrl}/update`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, parentId })
  }).then(res => res.json())
}

/**
 * 删除目录
 */
export function deleteDirectory(id) {
  return fetch(`${baseUrl}/delete/${id}`, {
    method: 'DELETE'
  }).then(res => res.json())
}

/**
 * 恢复目录
 */
export function restoreDirectory(id) {
  return fetch(`${baseUrl}/restore/${id}`, {
    method: 'POST'
  }).then(res => res.json())
}

/**
 * 搜索目录
 */
export function searchDirectories(keyword, userId = 1) {
  return fetch(`${baseUrl}/search?keyword=${encodeURIComponent(keyword)}&userId=${userId}`).then(res => res.json())
}

/**
 * 获取目录树
 */
export function getDirectoryTree(userId = 1) {
  return fetch(`${baseUrl}/tree?userId=${userId}`).then(res => res.json())
}

/**
 * 获取面包屑导航
 */
export function getBreadcrumb(id) {
  return fetch(`${baseUrl}/breadcrumb/${id}`).then(res => res.json())
}