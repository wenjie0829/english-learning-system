package com.englishlearning.app.service;

import com.englishlearning.app.dto.ImportExampleItem;
import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.User;
import com.englishlearning.app.entity.Word;
import com.englishlearning.app.repository.ExampleSentenceRepository;
import com.englishlearning.app.repository.UserRepository;
import com.englishlearning.app.repository.WordBookRepository;
import com.englishlearning.app.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;
    private final AiExampleGenerationService aiExampleGenerationService;

    public AdminService(UserRepository userRepository, WordRepository wordRepository,
                         WordBookRepository wordBookRepository, ExampleSentenceRepository exampleSentenceRepository,
                         AiExampleGenerationService aiExampleGenerationService) {
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.wordBookRepository = wordBookRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
        this.aiExampleGenerationService = aiExampleGenerationService;
    }

    // ---------- 用户管理 ----------

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUserRole(Long userId, User.UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    // ---------- 系统统计 ----------

    public Map<String, Object> getSystemStatistics() {
        List<User> allUsers = userRepository.findAll();
        long adminCount = allUsers.stream().filter(u -> u.getRole() == User.UserRole.ADMIN).count();
        long disabledCount = allUsers.stream().filter(u -> !Boolean.TRUE.equals(u.getEnabled())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", allUsers.size());
        stats.put("adminCount", adminCount);
        stats.put("studentCount", allUsers.size() - adminCount);
        stats.put("disabledCount", disabledCount);
        stats.put("totalWords", wordRepository.count());
        stats.put("totalWordBooks", wordBookRepository.count());
        stats.put("totalExampleSentences", exampleSentenceRepository.count());
        return stats;
    }

    // ---------- 批量生成例句 ----------

    /**
     * 给一批单词批量生成例句（AI 现生成，不依赖原文有没有例句）。
     * 已经有例句的单词会被跳过，不会重复叠加——避免每点一次都往同一个单词猛塞例句。
     * 每个单词单独调用一次 AI，某一个失败不影响其他单词继续处理。
     */
   // 就算前端已经做了分批调用，后端这里也加一道防线：
    // 万一哪天前端逻辑改了/被绕过，单次请求最多处理这么多个单词，
    // 避免一次性处理几十上百个单词导致请求长时间不返回、占用大量内存。
    private static final int MAX_BATCH_SIZE = 20;

    @Transactional
    public Map<String, Object> generateExamplesForWords(List<Long> wordIds, int countPerWord) {
        if (wordIds != null && wordIds.size() > MAX_BATCH_SIZE) {
            throw new RuntimeException("单次最多处理 " + MAX_BATCH_SIZE + " 个单词，请分批选择");
        }

        int generated = 0;
        int skippedHasExamples = 0;
        int failed = 0;
        int totalExamplesCreated = 0;

        if (wordIds != null) {
            for (Long wordId : wordIds) {
                Word word = wordRepository.findById(wordId).orElse(null);
                if (word == null) {
                    failed++;
                    continue;
                }

                List<ExampleSentence> existing = exampleSentenceRepository.findByWord(word);
                if (!existing.isEmpty()) {
                    skippedHasExamples++;
                    continue;
                }

                try {
                    List<ImportExampleItem> examples = aiExampleGenerationService.generateExamples(word, countPerWord);
                    for (ImportExampleItem ex : examples) {
                        ExampleSentence sentence = new ExampleSentence();
                        sentence.setWord(word);
                        sentence.setSentence(ex.getSentence());
                        sentence.setTranslation(ex.getTranslation());
                        sentence.setIsOriginal(false); // 标记为 AI 生成，非原文摘录
                        exampleSentenceRepository.save(sentence);
                        totalExamplesCreated++;
                    }
                    if (!examples.isEmpty()) {
                        generated++;
                    } else {
                        failed++;
                    }
                } catch (Exception e) {
                    failed++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("wordsGenerated", generated);
        result.put("wordsSkipped", skippedHasExamples);
        result.put("wordsFailed", failed);
        result.put("totalExamplesCreated", totalExamplesCreated);
        return result;
    }
}