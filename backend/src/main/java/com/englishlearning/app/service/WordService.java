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

    @Transactional(readOnly = true)
    public List<Word> getAllWords() {
        List<Word> words = wordRepository.findAll();
        attachExampleCounts(words);
        return words;
    }

    /**
     * 给单词批量填充例句数量（只读填充，不落库）。
     * 用一次 group by 查出所有单词的例句数，再在内存里对应上去，避免逐个单词查库。
     */
    private void attachExampleCounts(List<Word> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        Map<Long, Integer> counts = new HashMap<>();
        for (Object[] row : exampleSentenceRepository.countGroupByWord()) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        for (Word word : words) {
            word.setExampleCount(counts.getOrDefault(word.getId(), 0));
        }
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