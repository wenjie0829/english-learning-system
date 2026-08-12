package com.englishlearning.app.repository;

import com.englishlearning.app.entity.User;
import com.englishlearning.app.entity.WrongWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WrongWordRepository extends JpaRepository<WrongWord, Long> {
    Optional<WrongWord> findByUserAndWord(User user, com.englishlearning.app.entity.Word word);
    List<WrongWord> findByUserAndResolved(User user, Boolean resolved);
    List<WrongWord> findByUser(User user);

    // 用 JOIN FETCH 主动把关联的 word 一起查出来，避免 Controller 返回 JSON 时
    // 因为 Session 已关闭、懒加载字段访问不到而报错（open-in-view: false 场景下的经典坑）
    @Query("SELECT w FROM WrongWord w JOIN FETCH w.word WHERE w.user = :user AND w.resolved = :resolved")
    List<WrongWord> findByUserAndResolvedWithWord(@Param("user") User user, @Param("resolved") Boolean resolved);
}