package com.englishlearning.app.repository;

import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExampleSentenceRepository extends JpaRepository<ExampleSentence, Long> {
    List<ExampleSentence> findByWord(Word word);

    long countByWord(Word word);

    /**
     * 一次性统计每个单词的例句数，避免在单词列表里对 600+ 个单词逐个查库（N+1）。
     * 返回 [wordId, count] 的行集合。
     */
    @Query("select e.word.id, count(e) from ExampleSentence e group by e.word.id")
    List<Object[]> countGroupByWord();
}
