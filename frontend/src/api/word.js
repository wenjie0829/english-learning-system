import request from '@/utils/request'

export const getAllWords = () => {
  return request({
    url: '/words',
    method: 'get'
  })
}

export const getWordById = (id) => {
  return request({
    url: `/words/${id}`,
    method: 'get'
  })
}

export const searchWords = (keyword) => {
  return request({
    url: '/words/search',
    method: 'get',
    params: { keyword }
  })
}

export const getExampleSentences = (wordId) => {
  return request({
    url: `/words/${wordId}/examples`,
    method: 'get'
  })
}
