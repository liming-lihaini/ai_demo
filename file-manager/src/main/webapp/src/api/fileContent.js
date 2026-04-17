/**
 * 文件内容更新 API
 */

const baseUrl = '/api/file'

/**
 * 更新文件内容
 * @param {number} id - 文件ID
 * @param {string} content - 文件内容
 */
export function updateFileContent(id, content) {
  return fetch(`${baseUrl}/update-content`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, content })
  }).then(res => res.json())
}