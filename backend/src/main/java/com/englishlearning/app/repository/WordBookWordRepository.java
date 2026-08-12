package com.englishlearning.app.repository;

import com.englishlearning.app.entity.WordBook;
import com.englishlearning.app.entity.WordBookWord;
import com.englishlearning.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordBookWordRepository extends JpaRepository<WordBookWord, Long> {
    List<WordBookWord> findByWordBook(WordBook wordBook);
    List<WordBookWord> findByWordBookOrderByOrderIndex(WordBook wordBook);
}
