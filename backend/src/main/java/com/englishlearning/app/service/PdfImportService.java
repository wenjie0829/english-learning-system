package com.englishlearning.app.service;

import com.englishlearning.app.dto.ImportExampleItem;
import com.englishlearning.app.dto.ImportWordItem;
import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.Word;
import com.englishlearning.app.repository.ExampleSentenceRepository;
import com.englishlearning.app.repository.WordRepository;
import com.englishlearning.app.util.DocumentTextExtractor;
import com.englishlearning.app.util.PdfWordParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单词书导入服务。
 * 类名沿用 PdfImportService（历史原因），支持两条解析路径：
 *   - 规则解析：正则匹配（PdfWordParser），速度快、免费，但对排版规范度要求较高，不提取例句
 *   - AI 智能解析：调用 AiWordExtractionService（DeepSeek 等），排版再乱也能读懂，
 *     还能顺带提取配套例句，但更慢、需要 API Key、有调用成本
 * 两条路径都支持"上传文件"和"直接粘贴文本"两种输入方式。
 *
 * 性能说明：判断"候选单词是否已存在于数据库"时，用一次 IN 查询批量核对，
 * 而不是对每个单词单独查一次数据库——几千上万个单词的词表如果逐条查询，
 * 会产生大量数据库往返，很容易导致请求超时。
 */
@Service
public class PdfImportService {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".pdf", ".txt", ".docx");

    private final WordRepository wordRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;
    private final AiWordExtractionService aiWordExtractionService;

    public PdfImportService(WordRepository wordRepository,
                             ExampleSentenceRepository exampleSentenceRepository,
                             AiWordExtractionService aiWordExtractionService) {
        this.wordRepository = wordRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
        this.aiWordExtractionService = aiWordExtractionService;
    }

    // ================== 规则解析（正则） ==================

    public List<ImportWordItem> parsePdf(MultipartFile file) throws IOException {
        String text = extractTextValidated(file);
        List<ImportWordItem> items = PdfWordParser.parse(text);
        return markExistence(items);
    }

    public List<ImportWordItem> parseText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new RuntimeException("粘贴的文本内容为空");
        }
        List<ImportWordItem> items = PdfWordParser.parse(rawText);
        return markExistence(items);
    }

    // ================== AI 智能解析 ==================

    public Map<String, Object> parsePdfWithAi(MultipartFile file) throws IOException {
        String text = extractTextValidated(file);
        return runAiExtraction(text);
    }

    public Map<String, Object> parseTextWithAi(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new RuntimeException("粘贴的文本内容为空");
        }
        return runAiExtraction(rawText);
    }

    private Map<String, Object> runAiExtraction(String text) {
        AiWordExtractionService.AiExtractionResult result = aiWordExtractionService.extractWords(text);
        List<ImportWordItem> marked = markExistence(result.getItems());

        Map<String, Object> response = new HashMap<>();
        response.put("items", marked);
        response.put("truncated", result.isTruncated());
        response.put("totalChunks", result.getTotalChunks());
        response.put("processedChunks", result.getProcessedChunks());
        response.put("failedChunks", result.getFailedChunks());
        return response;
    }

    // ================== 公共部分 ==================

    private String extractTextValidated(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请上传文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || SUPPORTED_EXTENSIONS.stream().noneMatch(ext -> filename.toLowerCase(Locale.ROOT).endsWith(ext))) {
            throw new RuntimeException("只支持 PDF / TXT / DOCX 格式");
        }
        return DocumentTextExtractor.extract(file);
    }

    /**
     * 一次性批量查出这批单词里，哪些已经在数据库中存在，避免逐条查询导致的性能问题。
     */
    private List<ImportWordItem> markExistence(List<ImportWordItem> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }
        List<String> candidateWords = items.stream().map(ImportWordItem::getWord).collect(Collectors.toList());
        Set<String> existingWords = wordRepository.findByWordIn(candidateWords).stream()
                .map(Word::getWord)
                .collect(Collectors.toCollection(HashSet::new));

        for (ImportWordItem item : items) {
            item.setAlreadyExists(existingWords.contains(item.getWord()));
        }
        return items;
    }

    /**
     * 管理员在前端确认（可能已编辑）之后，真正写入数据库。
     * 已存在的单词默认跳过，不会覆盖。如果这条候选带了例句（只有 AI 解析路径会有），
     * 新建单词的同时把例句也一并写进 example_sentence 表。
     */
    @Transactional
    public Map<String, Object> confirmImport(List<ImportWordItem> items) {
        int imported = 0;
        int skipped = 0;
        int examplesImported = 0;

        if (items != null && !items.isEmpty()) {
            List<String> candidateWords = items.stream()
                    .map(ImportWordItem::getWord)
                    .filter(w -> w != null && !w.trim().isEmpty())
                    .collect(Collectors.toList());
            Set<String> existingWords = wordRepository.findByWordIn(candidateWords).stream()
                    .map(Word::getWord)
                    .collect(Collectors.toCollection(HashSet::new));

            for (ImportWordItem item : items) {
                if (item.getWord() == null || item.getWord().trim().isEmpty()) {
                    continue;
                }
                if (existingWords.contains(item.getWord())) {
                    skipped++;
                    continue;
                }

                Word word = new Word();
                word.setWord(item.getWord().trim());
                word.setPhonetic(emptyToNull(item.getPhonetic()));
                word.setDefinition(item.getDefinition() == null ? "" : item.getDefinition().trim());
                word.setPartOfSpeech(emptyToNull(item.getPartOfSpeech()));
                word.setDifficultyLevel(Word.DifficultyLevel.MEDIUM);

                Word savedWord = wordRepository.save(word);
                existingWords.add(savedWord.getWord());
                imported++;

                if (item.getExamples() != null) {
                    for (ImportExampleItem ex : item.getExamples()) {
                        if (ex.getSentence() == null || ex.getSentence().trim().isEmpty()) continue;

                        ExampleSentence exampleSentence = new ExampleSentence();
                        exampleSentence.setWord(savedWord);
                        exampleSentence.setSentence(ex.getSentence().trim());
                        exampleSentence.setTranslation(emptyToNull(ex.getTranslation()));
                        exampleSentence.setIsOriginal(true);
                        exampleSentenceRepository.save(exampleSentence);
                        examplesImported++;
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("examplesImported", examplesImported);
        return result;
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}