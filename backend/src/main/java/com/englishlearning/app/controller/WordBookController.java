package com.englishlearning.app.controller;

import com.englishlearning.app.entity.Word;
import com.englishlearning.app.entity.WordBook;
import com.englishlearning.app.entity.WordBookWord;
import com.englishlearning.app.service.WordBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wordbooks")
public class WordBookController {

    private final WordBookService wordBookService;

    public WordBookController(WordBookService wordBookService) {
        this.wordBookService = wordBookService;
    }

    @GetMapping
    public ResponseEntity<List<WordBook>> getAllWordBooks() {
        return ResponseEntity.ok(wordBookService.getAllWordBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordBook> getWordBookById(@PathVariable Long id) {
        return wordBookService.getWordBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WordBook> createWordBook(@RequestBody WordBook wordBook) {
        return ResponseEntity.ok(wordBookService.createWordBook(wordBook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordBook> updateWordBook(@PathVariable Long id, @RequestBody WordBook wordBook) {
        return ResponseEntity.ok(wordBookService.updateWordBook(id, wordBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWordBook(@PathVariable Long id) {
        wordBookService.deleteWordBook(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/words")
    public ResponseEntity<List<WordBookWord>> getWordsInBook(@PathVariable Long id) {
        return ResponseEntity.ok(wordBookService.getWordsInBook(id));
    }

    @PostMapping("/{id}/words")
    public ResponseEntity<WordBookWord> addWordToBook(@PathVariable Long id, @RequestParam Long wordId, @RequestParam(required = false) Integer orderIndex) {
        return ResponseEntity.ok(wordBookService.addWordToBook(id, wordId, orderIndex));
    }

    @DeleteMapping("/{id}/words/{wordId}")
    public ResponseEntity<Void> removeWordFromBook(@PathVariable Long id, @PathVariable Long wordId) {
        wordBookService.removeWordFromBook(id, wordId);
        return ResponseEntity.ok().build();
    }
}
