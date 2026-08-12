package com.englishlearning.app.service;

import com.englishlearning.app.entity.Word;
import com.englishlearning.app.entity.WordBook;
import com.englishlearning.app.entity.WordBookWord;
import com.englishlearning.app.repository.WordBookRepository;
import com.englishlearning.app.repository.WordBookWordRepository;
import com.englishlearning.app.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WordBookService {

    private final WordBookRepository wordBookRepository;
    private final WordBookWordRepository wordBookWordRepository;
    private final WordRepository wordRepository;

    public WordBookService(WordBookRepository wordBookRepository, 
                          WordBookWordRepository wordBookWordRepository,
                          WordRepository wordRepository) {
        this.wordBookRepository = wordBookRepository;
        this.wordBookWordRepository = wordBookWordRepository;
        this.wordRepository = wordRepository;
    }

    public List<WordBook> getAllWordBooks() {
        return wordBookRepository.findAll();
    }

    public Optional<WordBook> getWordBookById(Long id) {
        return wordBookRepository.findById(id);
    }

    @Transactional
    public WordBook createWordBook(WordBook wordBook) {
        return wordBookRepository.save(wordBook);
    }

    @Transactional
    public WordBook updateWordBook(Long id, WordBook wordBookDetails) {
        WordBook wordBook = wordBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WordBook not found"));

        wordBook.setName(wordBookDetails.getName());
        wordBook.setDescription(wordBookDetails.getDescription());
        wordBook.setCategory(wordBookDetails.getCategory());
        wordBook.setCoverUrl(wordBookDetails.getCoverUrl());

        return wordBookRepository.save(wordBook);
    }

    @Transactional
    public void deleteWordBook(Long id) {
        wordBookRepository.deleteById(id);
    }

    public List<WordBookWord> getWordsInBook(Long wordBookId) {
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new RuntimeException("WordBook not found"));
        return wordBookWordRepository.findByWordBookOrderByOrderIndex(wordBook);
    }

    @Transactional
    public WordBookWord addWordToBook(Long wordBookId, Long wordId, Integer orderIndex) {
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new RuntimeException("WordBook not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        WordBookWord wordBookWord = new WordBookWord();
        wordBookWord.setWordBook(wordBook);
        wordBookWord.setWord(word);
        wordBookWord.setOrderIndex(orderIndex != null ? orderIndex : 0);

        return wordBookWordRepository.save(wordBookWord);
    }

    @Transactional
    public void removeWordFromBook(Long wordBookId, Long wordId) {
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new RuntimeException("WordBook not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        wordBookWordRepository.findByWordBook(wordBook).stream()
                .filter(wbw -> wbw.getWord().equals(word))
                .findFirst()
                .ifPresent(wordBookWordRepository::delete);
    }
}
