package com.englishlearning.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String model;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AIService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成单词的AI详细释义
     */
    public Mono<String> generateWordDefinition(String word, String partOfSpeech) {
        String prompt = String.format(
            "请为英语单词 \"%s\" (%s) 生成详细的中文释义，包括：\n" +
            "1. 基本含义\n" +
            "2. 常见用法\n" +
            "3. 词源说明（如果有）\n" +
            "4. 记忆技巧\n" +
            "请用中文回答，格式清晰。",
            word, partOfSpeech != null ? partOfSpeech : "未知词性"
        );

        return callAI(prompt);
    }

    /**
     * 生成单词的例句
     */
    public Mono<List<String>> generateExampleSentences(String word, int count) {
        String prompt = String.format(
            "请为英语单词 \"%s\" 生成%d个地道的英语例句，要求：\n" +
            "1. 例句要实用、常见\n" +
            "2. 包含不同的使用场景\n" +
            "3. 难度适中\n" +
            "4. 每个例句单独一行\n" +
            "请只返回例句，不要包含翻译或解释。",
            word, count
        );

        return callAI(prompt).map(response -> {
            List<String> sentences = new ArrayList<>();
            String[] lines = response.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("-")) {
                    sentences.add(trimmed);
                }
            }
            return sentences;
        });
    }

    /**
     * 生成单词的发音指导
     */
    public Mono<String> generatePronunciationGuide(String word) {
        String prompt = String.format(
            "请为英语单词 \"%s\" 提供发音指导，包括：\n" +
            "1. 国际音标\n" +
            "2. 发音要点\n" +
            "3. 常见发音错误\n" +
            "请用中文回答。",
            word
        );

        return callAI(prompt);
    }

    /**
     * 调用AI API
     */
    private Mono<String> callAI(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RuntimeException("AI API密钥未配置"));
        }

        // 构建请求体
        String requestBody = buildRequestBody(prompt);

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractContentFromResponse)
                .onErrorResume(e -> {
                    // 返回默认响应，避免影响主流程
                    return Mono.just("AI服务暂时不可用，请稍后再试。");
                });
    }

    /**
     * 构建API请求体
     */
    private String buildRequestBody(String prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(new AIRequest(model, prompt));
        } catch (Exception e) {
            throw new RuntimeException("构建AI请求失败", e);
        }
    }

    /**
     * 从响应中提取内容
     */
    private String extractContentFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }
            return "AI响应格式错误";
        } catch (Exception e) {
            return "解析AI响应失败: " + e.getMessage();
        }
    }

    /**
     * AI请求体类
     */
    private static class AIRequest {
        private String model;
        private List<Message> messages;

        public AIRequest(String model, String content) {
            this.model = model;
            this.messages = List.of(new Message("user", content));
        }

        // Getters
        public String getModel() { return model; }
        public List<Message> getMessages() { return messages; }
    }

    /**
     * 消息类
     */
    private static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters
        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
