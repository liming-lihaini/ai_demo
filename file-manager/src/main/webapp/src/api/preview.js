import request from './request'

/**
 * 预览 API
 */

/**
 * 获取预览信息
 * @param {number} fileId 文件ID
 */
export function getPreviewInfo(fileId) {
    return request({
        url: `/preview/${fileId}`,
        method: 'get'
    })
}

/**
 * 获取文件内容流（用于 img/audio/video 标签）
 * @param {number} fileId 文件ID
 */
export function getPreviewContent(fileId) {
    return request({
        url: `/preview/content/${fileId}`,
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 获取 PDF 预览内容
 * @param {number} fileId 文件ID
 */
export function getPdfPreview(fileId) {
    return request({
        url: `/preview/pdf/${fileId}`,
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 获取文本内容（TXT/MD）
 * @param {number} fileId 文件ID
 */
export function getTextPreview(fileId) {
    return request({
        url: `/preview/text/${fileId}`,
        method: 'get'
    })
}

/**
 * 获取 HTML 预览内容（Office 文档）
 * @param {number} fileId 文件ID
 */
export function getHtmlPreview(fileId) {
    return request({
        url: `/preview/html/${fileId}`,
        method: 'get'
    })
}

/**
 * 获取缩略图
 * @param {number} fileId 文件ID
 */
export function getThumbnail(fileId) {
    return request({
        url: `/preview/thumb/${fileId}`,
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 检查是否为大型文件
 * @param {number} fileId 文件ID
 */
export function isLargeFile(fileId) {
    return request({
        url: `/preview/large/${fileId}`,
        method: 'get'
    })
}

/**
 * 批量获取预览信息
 * @param {number[]} fileIds 文件ID数组
 */
export function getBatchPreviewInfo(fileIds) {
    return request({
        url: `/preview/batch?ids=${fileIds.join(',')}`,
        method: 'get'
    })
}

/**
 * 获取文件 Blob URL（用于预览）
 * @param {number} fileId 文件ID
 * @returns {string} Blob URL
 */
export function getBlobUrl(fileId) {
    return `/api/preview/content/${fileId}`
}

/**
 * 根据文件类型获取对应的预览 URL
 * @param {string} previewType 预览类型
 * @param {number} fileId 文件ID
 * @returns {string} 预览 URL
 */
export function getPreviewUrl(previewType, fileId) {
    switch (previewType) {
        case 'pdf':
            return `/api/preview/content/${fileId}`
        case 'text':
            return `/api/preview/text/${fileId}`
        case 'html':
            return `/api/preview/html/${fileId}`
        case 'image':
        case 'audio':
        case 'video':
            return `/api/preview/content/${fileId}`
        default:
            return `/api/preview/content/${fileId}`
    }
}