package com.englishlearning.app.controller;

import com.englishlearning.app.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<Map<String, String>>> generateDefinition(
            @RequestParam String word,
            @RequestParam(required = false) String partOfSpeech) {
        return aiService.generateWordDefinition(word, partOfSpeech)
                .map(definition -> ResponseEntity.ok(Map.of(
                        "word", word,
                        "definition", definition
                )))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(Map.of(
                        "error", "生成释义失败: " + e.getMessage()
                ))));
    }

    @GetMapping("/examples")
    public Mono<ResponseEntity<Map<String, Object>>> generateExamples(
            @RequestParam String word,
            @RequestParam(defaultValue = "3") int count) {
        return aiService.generateExampleSentences(word, count)
                .map(examples -> ResponseEntity.ok(Map.of(
                        "word", word,
                        "examples", examples,
                        "count", examples.size()
                )))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(Map.of(
                        "error", "生成例句失败: " + e.getMessage()
                ))));
    }

    @GetMapping("/pronunciation")
    public Mono<ResponseEntity<Map<String, String>>> generatePronunciationGuide(
            @RequestParam String word) {
        return aiService.generatePronunciationGuide(word)
                .map(guide -> ResponseEntity.ok(Map.of(
                        "word", word,
                        "guide", guide
                )))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(Map.of(
                        "error", "生成发音指导失败: " + e.getMessage()
                ))));
    }
}
