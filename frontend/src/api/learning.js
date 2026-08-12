import request from '@/utils/request'

export const getUserLearningRecords = (userId) => {
  return request({
    url: `/learning/records/${userId}`,
    method: 'get'
  })
}

export const getDueReviews = (userId) => {
  return request({
    url: `/learning/due/${userId}`,
    method: 'get'
  })
}

export const startLearning = (userId, wordId) => {
  return request({
    url: '/learning/start',
    method: 'post',
    params: { userId, wordId }
  })
}

export const reviewWord = (userId, wordId, isCorrect) => {
  return request({
    url: '/learning/review',
    method: 'post',
    params: { userId, wordId, isCorrect }
  })
}

export const addToFavorites = (userId, wordId) => {
  return request({
    url: '/learning/favorites',
    method: 'post',
    params: { userId, wordId }
  })
}

export const removeFromFavorites = (userId, wordId) => {
  return request({
    url: '/learning/favorites',
    method: 'delete',
    params: { userId, wordId }
  })
}

export const getUserFavorites = (userId) => {
  return request({
    url: `/learning/favorites/${userId}`,
    method: 'get'
  })
}

export const getUserWrongWords = (userId) => {
  return request({
    url: `/learning/wrong-words/${userId}`,
    method: 'get'
  })
}

export const markWrongWordAsResolved = (userId, wordId) => {
  return request({
    url: '/learning/wrong-words/resolve',
    method: 'post',
    params: { userId, wordId }
  })
}

export const getUserStatistics = (userId) => {
  return request({
    url: `/learning/statistics/${userId}`,
    method: 'get'
  })
}
