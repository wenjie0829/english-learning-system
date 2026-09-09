package com.englishlearning.app.repository;

import com.englishlearning.app.entity.LearningDailyStatistics;
import com.englishlearning.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningDailyStatisticsRepository extends JpaRepository<LearningDailyStatistics, Long> {

    Optional<LearningDailyStatistics> findByUserAndStatDate(User user, LocalDate statDate);

    List<LearningDailyStatistics> findByStatDateBetween(LocalDate start, LocalDate end);

    List<LearningDailyStatistics> findByUserAndStatDateBetween(User user, LocalDate start, LocalDate end);

    List<LearningDailyStatistics> findByUserAndStatDateGreaterThanEqual(User user, LocalDate start);

    // 看板用：统计某一天有学习/复习行为的用户数（当天有记录即视为活跃/已打卡）
    @Query("SELECT COUNT(DISTINCT ls.user) FROM LearningDailyStatistics ls WHERE ls.statDate = :date")
    long countActiveUsersOn(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT ls.user) FROM LearningDailyStatistics ls")
    long countDistinctUsers();
}
