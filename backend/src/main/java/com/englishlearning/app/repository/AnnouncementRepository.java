package com.englishlearning.app.repository;

import com.englishlearning.app.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /** 用户端可见：仅取已发布的，最新的在前 */
    List<Announcement> findTop5ByEnabledTrueOrderByCreatedAtDesc();

    List<Announcement> findAllByOrderByCreatedAtDesc();
}
