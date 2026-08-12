package com.englishlearning.app.repository;

import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExampleSentenceRepository extends JpaRepository<ExampleSentence, Long> {
    List<ExampleSentence> findByWord(Word word);
}
