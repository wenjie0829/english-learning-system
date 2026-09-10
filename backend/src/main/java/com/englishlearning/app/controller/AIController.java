package com.englishlearning.app.controller;

import com.englishlearning.app.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/definition")
    public ResponseEntity<Map<String, String>> generateDefinition(
            @RequestParam String word,
            @RequestParam(required = false) String partOfSpeech) {
        try {
            String definition = aiService.generateWordDefinition(word, partOfSpeech)
                    .block(Duration.ofSeconds(20));
            return ResponseEntity.ok(Map.of(
                    "word", word,
                    "definition", definition
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "生成释义失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> generateExamples(
            @RequestParam String word,
            @RequestParam(defaultValue = "3") int count) {
        try {
            List<String> examples = aiService.generateExampleSentences(word, count)
                    .block(Duration.ofSeconds(20));
            return ResponseEntity.ok(Map.of(
                    "word", word,
                    "examples", examples,
                    "count", examples.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "生成例句失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/pronunciation")
    public ResponseEntity<Map<String, String>> generatePronunciationGuide(
            @RequestParam String word) {
        try {
            String guide = aiService.generatePronunciationGuide(word)
                    .block(Duration.ofSeconds(20));
            return ResponseEntity.ok(Map.of(
                    "word", word,
                    "guide", guide
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "生成发音指导失败: " + e.getMessage()
            ));
        }
    }
}
