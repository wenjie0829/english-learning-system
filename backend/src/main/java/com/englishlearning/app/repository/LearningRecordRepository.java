package com.englishlearning.app.repository;

import com.englishlearning.app.entity.LearningRecord;
import com.englishlearning.app.entity.User;
import com.englishlearning.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    Optional<LearningRecord> findByUserAndWord(User user, Word word);
    List<LearningRecord> findByUser(User user);
    List<LearningRecord> findByUserAndStatus(User user, LearningRecord.LearningStatus status);

    @Query("SELECT lr FROM LearningRecord lr WHERE lr.user = :user AND lr.nextReviewAt <= :now AND lr.status != 'MASTERED'")
    List<LearningRecord> findDueReviews(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.user = :user AND lr.status = 'MASTERED'")
    Long countMasteredWordsByUser(@Param("user") User user);

    @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.user = :user")
    Long countTotalLearningWordsByUser(@Param("user") User user);

    // 用 JOIN FETCH 主动加载 word，供"我的单词"这类需要展示单词内容的列表页使用，
    // 避免 open-in-view: false 场景下懒加载字段序列化时报错
    @Query("SELECT lr FROM LearningRecord lr JOIN FETCH lr.word WHERE lr.user = :user ORDER BY lr.updatedAt DESC")
    List<LearningRecord> findByUserWithWord(@Param("user") User user);

    @Query("SELECT lr FROM LearningRecord lr JOIN FETCH lr.word WHERE lr.user = :user AND lr.status = :status ORDER BY lr.updatedAt DESC")
    List<LearningRecord> findByUserAndStatusWithWord(@Param("user") User user, @Param("status") LearningRecord.LearningStatus status);

    @Query("SELECT lr FROM LearningRecord lr JOIN FETCH lr.word WHERE lr.user = :user AND lr.nextReviewAt <= :now AND lr.status != 'MASTERED' ORDER BY lr.nextReviewAt ASC")
    List<LearningRecord> findDueReviewsWithWord(@Param("user") User user, @Param("now") LocalDateTime now);
}