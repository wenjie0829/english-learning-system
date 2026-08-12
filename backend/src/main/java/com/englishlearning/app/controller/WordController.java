package com.englishlearning.app.controller;

import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.Word;
import com.englishlearning.app.service.WordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping
    public ResponseEntity<List<Word>> getAllWords() {
        return ResponseEntity.ok(wordService.getAllWords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWordById(@PathVariable Long id) {
        return wordService.getWordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Word>> searchWords(@RequestParam String keyword) {
        return ResponseEntity.ok(wordService.searchWords(keyword));
    }

    @GetMapping("/difficulty/{level}")
    public ResponseEntity<List<Word>> getWordsByDifficulty(@PathVariable Word.DifficultyLevel level) {
        return ResponseEntity.ok(wordService.getWordsByDifficulty(level));
    }

    @PostMapping
    public ResponseEntity<Word> createWord(@RequestBody Word word) {
        return ResponseEntity.ok(wordService.createWord(word));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Word> updateWord(@PathVariable Long id, @RequestBody Word word) {
        return ResponseEntity.ok(wordService.updateWord(id, word));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/examples")
    public ResponseEntity<List<ExampleSentence>> getExampleSentences(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getExampleSentences(id));
    }

    @PostMapping("/{id}/examples")
    public ResponseEntity<ExampleSentence> addExampleSentence(@PathVariable Long id, @RequestBody ExampleSentence exampleSentence) {
        return ResponseEntity.ok(wordService.addExampleSentence(id, exampleSentence));
    }
}
