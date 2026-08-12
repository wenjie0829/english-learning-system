package com.englishlearning.app.repository;

import com.englishlearning.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByWord(String word);
    List<Word> findByDifficultyLevel(Word.DifficultyLevel difficultyLevel);
    boolean existsByWord(String word);

    // 批量查询：一次性查出这批候选单词里，哪些已经存在于数据库。
    // 用于替代"逐个单词单独查一次数据库"的写法，避免几千上万个单词导入时产生 N+1 查询导致超时。
    List<Word> findByWordIn(Collection<String> words);

    @Query("SELECT w FROM Word w WHERE w.word LIKE %:keyword% OR w.definition LIKE %:keyword%")
    List<Word> searchByKeyword(@Param("keyword") String keyword);
}