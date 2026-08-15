import request from '@/utils/request'

// ---------- 系统统计 ----------
export const getSystemStatistics = () => {
  return request({ url: '/admin/statistics', method: 'get' })
}

// ---------- 用户管理 ----------
export const getAllUsers = () => {
  return request({ url: '/admin/users', method: 'get' })
}

export const updateUserRole = (userId, role) => {
  return request({
    url: `/admin/users/${userId}/role`,
    method: 'put',
    params: { role }
  })
}

export const updateUserStatus = (userId, enabled) => {
  return request({
    url: `/admin/users/${userId}/status`,
    method: 'put',
    params: { enabled }
  })
}

export const deleteUser = (userId) => {
  return request({ url: `/admin/users/${userId}`, method: 'delete' })
}

// ---------- 单词库管理 ----------
export const getAdminWords = () => {
  return request({ url: '/admin/words', method: 'get' })
}

export const createWord = (data) => {
  return request({ url: '/admin/words', method: 'post', data })
}

export const updateWord = (id, data) => {
  return request({ url: `/admin/words/${id}`, method: 'put', data })
}

export const deleteWord = (id) => {
  return request({ url: `/admin/words/${id}`, method: 'delete' })
}

export const getExampleSentences = (wordId) => {
  return request({ url: `/admin/words/${wordId}/examples`, method: 'get' })
}

export const addExampleSentence = (wordId, data) => {
  return request({ url: `/admin/words/${wordId}/examples`, method: 'post', data })
}

export const deleteExampleSentence = (exampleId) => {
  return request({ url: `/admin/words/examples/${exampleId}`, method: 'delete' })
}

export const batchDeleteWords = (ids) => {
  return request({ url: '/admin/words/batch', method: 'delete', data: ids })
}

// 单次请求最多处理20个单词（跟后端 MAX_BATCH_SIZE 对应），超时给足余量
export const generateExamplesForWords = (wordIds, count = 3) => {
  return request({
    url: '/admin/words/generate-examples',
    method: 'post',
    data: { wordIds, count },
    timeout: 150000
  })
}

// ---------- PDF 单词书导入 ----------
export const parsePdfImport = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/import/pdf',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000 // 大词表（几千上万个单词）解析耗时较长，单独放宽到60秒
  })
}

export const confirmPdfImport = (items) => {
  return request({ url: '/admin/import/confirm', method: 'post', data: items })
}

export const parseTextImport = (text) => {
  return request({ url: '/admin/import/text', method: 'post', data: { text }, timeout: 60000 })
}

// ---------- AI 智能解析（DeepSeek 等）----------
// AI 逐段调用大模型，文档越大耗时越久，超时时间放宽到 5 分钟
export const parseAiFileImport = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/import/ai/pdf',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000
  })
}

export const parseAiTextImport = (text) => {
  return request({ url: '/admin/import/ai/text', method: 'post', data: { text }, timeout: 300000 })
}
