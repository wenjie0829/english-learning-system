package com.englishlearning.app.service;

import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.Word;
import com.englishlearning.app.repository.ExampleSentenceRepository;
import com.englishlearning.app.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WordService {

    private final WordRepository wordRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;

    public WordService(WordRepository wordRepository, ExampleSentenceRepository exampleSentenceRepository) {
        this.wordRepository = wordRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
    }

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public Optional<Word> getWordById(Long id) {
        return wordRepository.findById(id);
    }

    public Optional<Word> getWordByWord(String word) {
        return wordRepository.findByWord(word);
    }

    public List<Word> searchWords(String keyword) {
        return wordRepository.searchByKeyword(keyword);
    }

    public List<Word> getWordsByDifficulty(Word.DifficultyLevel difficultyLevel) {
        return wordRepository.findByDifficultyLevel(difficultyLevel);
    }

    @Transactional
    public Word createWord(Word word) {
        return wordRepository.save(word);
    }

    @Transactional
    public Word updateWord(Long id, Word wordDetails) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        word.setWord(wordDetails.getWord());
        word.setPhonetic(wordDetails.getPhonetic());
        word.setDefinition(wordDetails.getDefinition());
        word.setAiDefinition(wordDetails.getAiDefinition());
        word.setPartOfSpeech(wordDetails.getPartOfSpeech());
        word.setDifficultyLevel(wordDetails.getDifficultyLevel());
        word.setAudioUrl(wordDetails.getAudioUrl());

        return wordRepository.save(word);
    }

    @Transactional
    public void deleteWord(Long id) {
        wordRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> deleteWords(List<Long> ids) {
        int deleted = 0;
        for (Long id : ids) {
            if (wordRepository.existsById(id)) {
                wordRepository.deleteById(id);
                deleted++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("deleted", deleted);
        return result;
    }

    public List<ExampleSentence> getExampleSentences(Long wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));
        return exampleSentenceRepository.findByWord(word);
    }

    @Transactional
    public ExampleSentence addExampleSentence(Long wordId, ExampleSentence exampleSentence) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));
        exampleSentence.setWord(word);
        return exampleSentenceRepository.save(exampleSentence);
    }

    @Transactional
    public void deleteExampleSentence(Long exampleSentenceId) {
        if (!exampleSentenceRepository.existsById(exampleSentenceId)) {
            throw new RuntimeException("Example sentence not found");
        }
        exampleSentenceRepository.deleteById(exampleSentenceId);
    }

    public boolean existsByWord(String word) {
        return wordRepository.existsByWord(word);
    }
}