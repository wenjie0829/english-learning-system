package com.englishlearning.app.repository;

import com.englishlearning.app.entity.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordBookRepository extends JpaRepository<WordBook, Long> {
    List<WordBook> findByCategory(String category);
}
