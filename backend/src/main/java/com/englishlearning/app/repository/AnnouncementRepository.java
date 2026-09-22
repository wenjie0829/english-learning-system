package com.englishlearning.app.repository;

import com.englishlearning.app.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /** 首页公告面板：仅取已发布的，最新的在前，最多 5 条 */
    List<Announcement> findTop5ByEnabledTrueOrderByCreatedAtDesc();

    /** 公告列表页：全部已发布的，最新的在前（不做条数截断） */
    List<Announcement> findByEnabledTrueOrderByCreatedAtDesc();

    List<Announcement> findAllByOrderByCreatedAtDesc();
}
