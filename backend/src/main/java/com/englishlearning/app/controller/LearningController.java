package com.englishlearning.app.controller;

import com.englishlearning.app.entity.Favorite;
import com.englishlearning.app.entity.LearningRecord;
import com.englishlearning.app.entity.WrongWord;
import com.englishlearning.app.service.LearningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/records/{userId}")
    public ResponseEntity<List<LearningRecord>> getUserLearningRecords(@PathVariable Long userId) {
        return ResponseEntity.ok(learningService.getUserLearningRecords(userId));
    }

    @GetMapping("/due/{userId}")
    public ResponseEntity<List<LearningRecord>> getDueReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(learningService.getDueReviews(userId));
    }

    @PostMapping("/start")
    public ResponseEntity<LearningRecord> startLearning(@RequestParam Long userId, @RequestParam Long wordId) {
        return ResponseEntity.ok(learningService.startLearning(userId, wordId));
    }

    @PostMapping("/review")
    public ResponseEntity<LearningRecord> reviewWord(@RequestParam Long userId, @RequestParam Long wordId, @RequestParam boolean isCorrect) {
        return ResponseEntity.ok(learningService.reviewWord(userId, wordId, isCorrect));
    }

    @PostMapping("/favorites")
    public ResponseEntity<Void> addToFavorites(@RequestParam Long userId, @RequestParam Long wordId) {
        learningService.addToFavorites(userId, wordId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<Void> removeFromFavorites(@RequestParam Long userId, @RequestParam Long wordId) {
        learningService.removeFromFavorites(userId, wordId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(learningService.getUserFavorites(userId));
    }

    @PostMapping("/wrong-words")
    public ResponseEntity<Void> addToWrongWords(@RequestParam Long userId, @RequestParam Long wordId) {
        learningService.addToWrongWords(userId, wordId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/wrong-words/resolve")
    public ResponseEntity<Void> markWrongWordAsResolved(@RequestParam Long userId, @RequestParam Long wordId) {
        learningService.markWrongWordAsResolved(userId, wordId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wrong-words/{userId}")
    public ResponseEntity<List<WrongWord>> getUserWrongWords(@PathVariable Long userId) {
        return ResponseEntity.ok(learningService.getUserWrongWords(userId));
    }

    @GetMapping("/statistics/{userId}")
    public ResponseEntity<LearningService.LearningStatistics> getUserStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(learningService.getUserStatistics(userId));
    }
}
