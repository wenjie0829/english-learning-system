package com.englishlearning.app.service;

import com.englishlearning.app.dto.ImportExampleItem;
import com.englishlearning.app.entity.Word;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 给已有单词批量"生成"例句（不是从原文提取，是让 AI 直接现造）。
 * 用途：解决"原始材料没有例句，或者只想快速给单词库补上例句"这个需求，
 * 跟 AiWordExtractionService（导入时顺带提取原文例句）是两码事，职责分开。
 *
 * 复用同一套 ai.* 配置（api-key / api-url / model），跟单词书导入用的是同一个 DeepSeek Key。
 */
@Service
public class AiExampleGenerationService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:deepseek-v4-flash}")
    private String model;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AiExampleGenerationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30_000);
        requestFactory.setReadTimeout(60_000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * 给一个单词生成指定条数的例句（英文例句 + 中文翻译）。
     */
    public List<ImportExampleItem> generateExamples(Word word, int count) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your-ai-api-key")) {
            throw new RuntimeException("AI 服务未配置：请先在 application.yml 的 ai.api-key 里填入你的 DeepSeek API Key");
        }

        String systemPrompt = "你是一个英语教学助手，负责给英语单词生成地道、自然、贴近日常使用场景的例句。"
                + "要求：例句要能体现这个单词的典型用法和这个具体释义，长度适中（一句话，不要太长太复杂），"
                + "不同例句之间场景要有变化，不要重复相似的句式。每条例句配一句准确的中文翻译。"
                + "只输出一个 JSON 对象，不要输出任何其他说明文字，不要用 markdown 代码块包裹。"
                + "JSON 格式严格如下：{\"examples\":[{\"sentence\":\"英文例句\",\"translation\":\"中文翻译\"}]}。";

        String userPrompt = String.format(
                "单词：%s\n音标：%s\n词性：%s\n中文释义：%s\n请生成 %d 条例句。",
                word.getWord(),
                word.getPhonetic() == null ? "无" : word.getPhonetic(),
                word.getPartOfSpeech() == null ? "无" : word.getPartOfSpeech(),
                word.getDefinition() == null ? "无" : word.getDefinition(),
                count
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RuntimeException("AI 返回结果格式异常");
            }
            String content = choices.get(0).path("message").path("content").asText();
            content = stripMarkdownFence(content);

            JsonNode parsed = objectMapper.readTree(content);
            JsonNode examplesNode = parsed.path("examples");
            List<ImportExampleItem> result = new ArrayList<>();
            if (examplesNode.isArray()) {
                for (JsonNode node : examplesNode) {
                    String sentence = textOrEmpty(node, "sentence");
                    if (sentence.isBlank()) continue;
                    result.add(new ImportExampleItem(sentence, textOrEmpty(node, "translation")));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("为单词「" + word.getWord() + "」生成例句失败：" + e.getMessage());
        }
    }

    private String textOrEmpty(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private String stripMarkdownFence(String content) {
        if (content == null) return "{}";
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "");
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }
}