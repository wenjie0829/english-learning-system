package com.englishlearning.app.service;

import com.englishlearning.app.entity.Announcement;
import com.englishlearning.app.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    // ---------- 用户端 ----------

    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findTop5ByEnabledTrueOrderByCreatedAtDesc();
    }

    // ---------- 管理端 ----------

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Announcement createAnnouncement(String title, String content, Boolean enabled) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setEnabled(enabled == null || enabled);
        return announcementRepository.save(announcement);
    }

    @Transactional
    public Announcement updateAnnouncement(Long id, String title, String content, Boolean enabled) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        if (title != null) announcement.setTitle(title);
        if (content != null) announcement.setContent(content);
        if (enabled != null) announcement.setEnabled(enabled);
        return announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new RuntimeException("公告不存在");
        }
        announcementRepository.deleteById(id);
    }
}
