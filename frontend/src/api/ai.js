import request from '@/utils/request'

export const generateDefinition = (word, partOfSpeech) => {
  return request({
    url: '/ai/definition',
    method: 'get',
    params: { word, partOfSpeech }
  })
}

export const generateExamples = (word, count = 3) => {
  return request({
    url: '/ai/examples',
    method: 'get',
    params: { word, count }
  })
}

export const generatePronunciationGuide = (word) => {
  return request({
    url: '/ai/pronunciation',
    method: 'get',
    params: { word }
  })
}
