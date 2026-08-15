package com.englishlearning.app.service;

import com.englishlearning.app.dto.ImportExampleItem;
import com.englishlearning.app.dto.ImportWordItem;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用 AI（默认接 DeepSeek，OpenAI 兼容协议）从任意排版的文本里"读懂"并提取单词条目（含配套例句），
 * 用来替代/补充 PdfWordParser 那套基于正则的死板匹配，解决"排版稍微复杂就解析失败"的问题。
 *
 * 用法：把长文本切成若干小段（避免超出模型单次请求的长度限制），
 * 每段单独请求一次 AI，要求它只返回结构化 JSON，最后把所有段的结果合并去重。
 *
 * 配置来自 application.yml 的 ai.* 三项：
 *   ai.api-key  — 你的 API Key
 *   ai.api-url  — 完整的 chat completions 接口地址，例如 DeepSeek 是 https://api.deepseek.com/chat/completions
 *   ai.model    — 模型名，DeepSeek 目前是 deepseek-v4-flash（便宜快）或 deepseek-v4-pro（更准但更贵）
 */
@Service
public class AiWordExtractionService {

    // 每段送去给 AI 的文本长度上限（字符数）。太大容易超出模型上下文，也会让单次请求变慢变贵。
    private static final int CHUNK_CHAR_LIMIT = 2500;
    // 最多处理多少段，防止超大文档（几百页的书）导致处理时间和费用失控。
    private static final int MAX_CHUNKS = 30;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:deepseek-v4-flash}")
    private String model;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AiWordExtractionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30_000);
        requestFactory.setReadTimeout(60_000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public static class AiExtractionResult {
        private final List<ImportWordItem> items;
        private final boolean truncated;
        private final int totalChunks;
        private final int processedChunks;
        private final int failedChunks;

        public AiExtractionResult(List<ImportWordItem> items, boolean truncated, int totalChunks, int processedChunks, int failedChunks) {
            this.items = items;
            this.truncated = truncated;
            this.totalChunks = totalChunks;
            this.processedChunks = processedChunks;
            this.failedChunks = failedChunks;
        }

        public List<ImportWordItem> getItems() { return items; }
        public boolean isTruncated() { return truncated; }
        public int getTotalChunks() { return totalChunks; }
        public int getProcessedChunks() { return processedChunks; }
        public int getFailedChunks() { return failedChunks; }
    }

    public AiExtractionResult extractWords(String rawText) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your-ai-api-key")) {
            throw new RuntimeException("AI 服务未配置：请先在 application.yml 的 ai.api-key 里填入你的 DeepSeek API Key");
        }

        List<String> chunks = splitIntoChunks(rawText);
        boolean truncated = chunks.size() > MAX_CHUNKS;
        int totalChunks = chunks.size();
        List<String> chunksToProcess = truncated ? chunks.subList(0, MAX_CHUNKS) : chunks;

        Map<String, ImportWordItem> merged = new LinkedHashMap<>();
        int failedChunks = 0;

        for (String chunk : chunksToProcess) {
            try {
                List<ImportWordItem> parsed = callAiForChunk(chunk);
                for (ImportWordItem item : parsed) {
                    if (item.getWord() == null || item.getWord().isBlank()) continue;
                    String key = item.getWord().trim().toLowerCase();
                    merged.putIfAbsent(key, item);
                }
            } catch (Exception e) {
                // 单段失败不影响其他段，累计失败次数告知前端即可
                failedChunks++;
            }
        }

        return new AiExtractionResult(
                new ArrayList<>(merged.values()),
                truncated,
                totalChunks,
                chunksToProcess.size(),
                failedChunks
        );
    }

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        String[] lines = text.split("\\r?\\n");
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (current.length() + line.length() + 1 > CHUNK_CHAR_LIMIT && current.length() > 0) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }
        if (current.length() > 0) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private List<ImportWordItem> callAiForChunk(String chunkText) throws Exception {
        String systemPrompt = "你是一个专业的英语词汇提取助手。你会收到一段可能来自单词书、词典、课本或其他学习资料的文本，"
                + "文本的排版可能不规则，可能夹杂说明文字、页码、标题等无关内容。"
                + "你的任务是从中找出所有'英语单词或短语'及其对应的音标、词性、中文释义，以及原文中紧跟着这个单词出现的例句（如果有的话）。"
                + "忽略与单词学习无关的内容。如果某一项信息文本中没有提供（比如没有音标），对应字段留空字符串即可，不要编造。"
                + "例句同理：只提取原文里真实出现、明确对应这个单词的例句，最多3条；原文没有配例句就把 examples 留空数组，绝对不要自己编造例句。"
                + "只输出一个 JSON 对象，不要输出任何其他说明文字，不要用 markdown 代码块包裹。"
                + "JSON 格式严格如下：{\"items\":[{\"word\":\"单词\",\"phonetic\":\"音标\",\"partOfSpeech\":\"词性缩写如n./v./adj.\",\"definition\":\"中文释义\","
                + "\"examples\":[{\"sentence\":\"英文例句\",\"translation\":\"中文翻译\"}]}]}。"
                + "如果这段文本里没有任何可提取的单词，返回 {\"items\":[]}。";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", chunkText)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException("AI 返回结果格式异常，没有 choices 字段");
        }
        String content = choices.get(0).path("message").path("content").asText();
        content = stripMarkdownFence(content);

        JsonNode parsed = objectMapper.readTree(content);
        JsonNode itemsNode = parsed.path("items");
        List<ImportWordItem> result = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                ImportWordItem item = new ImportWordItem();
                item.setWord(textOrEmpty(node, "word"));
                item.setPhonetic(textOrEmpty(node, "phonetic"));
                item.setPartOfSpeech(textOrEmpty(node, "partOfSpeech"));
                item.setDefinition(textOrEmpty(node, "definition"));
                item.setAlreadyExists(false);

                List<ImportExampleItem> examples = new ArrayList<>();
                JsonNode examplesNode = node.path("examples");
                if (examplesNode.isArray()) {
                    for (JsonNode exNode : examplesNode) {
                        String sentence = textOrEmpty(exNode, "sentence");
                        if (sentence.isBlank()) continue;
                        examples.add(new ImportExampleItem(sentence, textOrEmpty(exNode, "translation")));
                    }
                }
                item.setExamples(examples);

                result.add(item);
            }
        }
        return result;
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