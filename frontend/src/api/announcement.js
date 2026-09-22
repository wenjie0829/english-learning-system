import request from '@/utils/request'

// 首页公告面板：只读取「启用中」的公告，后端最多返回 5 条
export const getActiveAnnouncements = () => {
  return request({
    url: '/announcements/active',
    method: 'get'
  })
}

// 公告列表页：读取全部「启用中」的公告（不截断），按发布时间倒序
export const getAnnouncementList = () => {
  return request({
    url: '/announcements',
    method: 'get'
  })
}
