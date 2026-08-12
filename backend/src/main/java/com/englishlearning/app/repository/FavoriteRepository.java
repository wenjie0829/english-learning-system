package com.englishlearning.app.repository;

import com.englishlearning.app.entity.Favorite;
import com.englishlearning.app.entity.User;
import com.englishlearning.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndWord(User user, Word word);
    List<Favorite> findByUser(User user);
    boolean existsByUserAndWord(User user, Word word);

    // 同样用 JOIN FETCH 主动加载 word，避免收藏列表页出现和错词本一样的懒加载崩溃
    @Query("SELECT f FROM Favorite f JOIN FETCH f.word WHERE f.user = :user")
    List<Favorite> findByUserWithWord(@Param("user") User user);
}