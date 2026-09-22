package com.englishlearning.app.controller;

import com.englishlearning.app.entity.Announcement;
import com.englishlearning.app.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端公告接口：只返回已发布的公告，登录用户可见。
 * 管理端的公告维护接口在 AdminController 中，路径 /api/admin/announcements。
 */
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** 首页公告面板：只返回最新的 5 条 */
    @GetMapping("/active")
    public ResponseEntity<List<Announcement>> getActiveAnnouncements() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }

    /** 公告列表页：返回全部启用中的公告，按发布时间倒序 */
    @GetMapping
    public ResponseEntity<List<Announcement>> getAnnouncementList() {
        return ResponseEntity.ok(announcementService.getAllActiveAnnouncements());
    }
}
