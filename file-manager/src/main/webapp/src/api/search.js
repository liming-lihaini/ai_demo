/**
 * 搜索 API
 * 对应后端 SearchController 接口
 */

const baseUrl = '/api/search'

/**
 * 搜索文件和目录
 * @param {Object} params - 搜索参数
 * @param {string} params.q - 搜索关键词
 * @param {string} params.type - 搜索类型: file/directory/all
 * @param {number} params.userId - 用户ID
 * @param {string} params.fileType - 文件类型筛选
 * @param {number} params.startDate - 开始日期
 * @param {number} params.endDate - 结束日期
 * @param {number} params.minSize - 最小文件大小
 * @param {number} params.maxSize - 最大文件大小
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 */
export function search(params) {
  const queryParams = new URLSearchParams()
  if (params.q) queryParams.append('q', params.q)
  if (params.type) queryParams.append('type', params.type || 'all')
  if (params.userId) queryParams.append('userId', params.userId)
  if (params.fileType) queryParams.append('fileType', params.fileType)
  if (params.startDate) queryParams.append('startDate', params.startDate)
  if (params.endDate) queryParams.append('endDate', params.endDate)
  if (params.minSize) queryParams.append('minSize', params.minSize)
  if (params.maxSize) queryParams.append('maxSize', params.maxSize)
  queryParams.append('page', params.page || 1)
  queryParams.append('size', params.size || 20)

  return fetch(`${baseUrl}?${queryParams.toString()}`).then(res => res.json())
}

/**
 * 获取搜索建议（自动补全）
 * @param {string} keyword - 搜索关键词
 * @param {number} userId - 用户ID
 * @param {number} limit - 返回数量限制
 */
export function getSuggestions(keyword, userId = 1, limit = 10) {
  return fetch(`${baseUrl}/suggest?q=${encodeURIComponent(keyword)}&userId=${userId}&limit=${limit}`)
    .then(res => res.json())
}

/**
 * 获取搜索历史
 */
export function getSearchHistory() {
  const history = localStorage.getItem('searchHistory')
  return history ? JSON.parse(history) : []
}

/**
 * 保存搜索历史
 * @param {string} keyword - 搜索关键词
 */
export function saveSearchHistory(keyword) {
  if (!keyword || keyword.trim() === '') return

  let history = getSearchHistory()
  // 去除重复，保留最新的
  history = history.filter(item => item !== keyword)
  history.unshift(keyword)
  // 只保留最近10条
  history = history.slice(0, 10)
  localStorage.setItem('searchHistory', JSON.stringify(history))
}

/**
 * 清空搜索历史
 */
export function clearSearchHistory() {
  localStorage.removeItem('searchHistory')
}

/**
 * 删除单条搜索历史
 * @param {string} keyword - 搜索关键词
 */
export function removeSearchHistory(keyword) {
  let history = getSearchHistory()
  history = history.filter(item => item !== keyword)
  localStorage.setItem('searchHistory', JSON.stringify(history))
}
