/**
 * 文件管理 API
 * 对应后端 FileController 接口
 */

const baseUrl = '/api/file'

/**
 * 创建 Markdown 文件（带模板内容）
 * @param {string} name - 文件名
 * @param {number} parentId - 父目录ID
 * @param {number} userId - 用户ID
 */
export function createMdFile(name, parentId = 0, userId = 1) {
  return fetch(`${baseUrl}/create-md?name=${encodeURIComponent(name)}&parentId=${parentId}&userId=${userId}`, {
    method: 'POST'
  }).then(res => res.json())
}

/**
 * 上传文件
 * @param {File} file - 文件对象
 * @param {number} parentId - 父目录ID
 * @param {number} userId - 用户ID
 */
export function uploadFile(file, parentId = 0, userId = 1) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('parentId', parentId)
  formData.append('userId', userId)

  return fetch(`${baseUrl}/upload?parentId=${parentId}&userId=${userId}`, {
    method: 'POST',
    body: formData
  }).then(res => res.json())
}

/**
 * 获取文件列表
 * @param {number} parentId - 父目录ID
 * @param {number} userId - 用户ID
 */
export function getFileList(parentId = 0, userId = 1) {
  return fetch(`${baseUrl}/list?parentId=${parentId}&userId=${userId}`).then(res => res.json())
}

/**
 * 搜索文件
 * @param {string} keyword - 搜索关键字
 * @param {number} userId - 用户ID
 */
export function searchFiles(keyword, userId = 1) {
  return fetch(`${baseUrl}/search?keyword=${encodeURIComponent(keyword)}&userId=${userId}`).then(res => res.json())
}

/**
 * 删除文件（软删除）
 * @param {number} id - 文件ID
 */
export function deleteFile(id) {
  return fetch(`${baseUrl}/delete/${id}`, {
    method: 'DELETE'
  }).then(res => res.json())
}

/**
 * 批量删除文件
 * @param {number[]} ids - 文件ID数组
 */
export function deleteFiles(ids) {
  return fetch(`${baseUrl}/delete-batch?ids=${ids.join(',')}`, {
    method: 'DELETE'
  }).then(res => res.json())
}

/**
 * 恢复文件
 * @param {number} id - 文件ID
 */
export function restoreFile(id) {
  return fetch(`${baseUrl}/restore/${id}`, {
    method: 'POST'
  }).then(res => res.json())
}

/**
 * 重命名文件
 * @param {number} id - 文件ID
 * @param {string} name - 新名称
 */
export function renameFile(id, name) {
  return fetch(`${baseUrl}/rename`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, name })
  }).then(res => res.json())
}

/**
 * 移动文件
 * @param {number} id - 文件ID
 * @param {number} parentId - 目标目录ID
 */
export function moveFile(id, parentId) {
  return fetch(`${baseUrl}/move`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, parentId })
  }).then(res => res.json())
}

/**
 * 下载文件
 * @param {number} id - 文件ID
 * @returns {Promise<Blob>}
 */
export function downloadFile(id) {
  return fetch(`${baseUrl}/download/${id}`).then(res => res.blob())
}

/**
 * 获取文件下载URL
 * @param {number} id - 文件ID
 */
export function getDownloadUrl(id) {
  return `${baseUrl}/download/${id}`
}