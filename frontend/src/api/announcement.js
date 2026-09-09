import request from '@/utils/request'

// 面向用户端：只读取「启用中」的公告（无需登录态以外的权限）
export const getActiveAnnouncements = () => {
  return request({
    url: '/announcements/active',
    method: 'get'
  })
}
