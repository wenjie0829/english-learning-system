package com.englishlearning.app.service;

import com.englishlearning.app.dto.ImportExampleItem;
import com.englishlearning.app.entity.*;
import com.englishlearning.app.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;
    private final AiExampleGenerationService aiExampleGenerationService;
    private final LearningRecordRepository learningRecordRepository;
    private final FavoriteRepository favoriteRepository;
    private final WrongWordRepository wrongWordRepository;
    private final LearningDailyStatisticsRepository dailyStatisticsRepository;

    public AdminService(UserRepository userRepository, WordRepository wordRepository,
                         WordBookRepository wordBookRepository, ExampleSentenceRepository exampleSentenceRepository,
                         AiExampleGenerationService aiExampleGenerationService,
                         LearningRecordRepository learningRecordRepository,
                         FavoriteRepository favoriteRepository,
                         WrongWordRepository wrongWordRepository,
                         LearningDailyStatisticsRepository dailyStatisticsRepository) {
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.wordBookRepository = wordBookRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
        this.aiExampleGenerationService = aiExampleGenerationService;
        this.learningRecordRepository = learningRecordRepository;
        this.favoriteRepository = favoriteRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.dailyStatisticsRepository = dailyStatisticsRepository;
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

    // ================== 管理端数据可视化看板 ==================

    /**
     * 管理端首页看板数据：
     * 1) 基础总量数字（复用 getSystemStatistics）
     * 2) 今日活跃 / 今日打卡 / 今日学习量
     * 3) 近14天：每日活跃用户数、新学词数、复习词数（折线/柱状图）
     * 4) 近14天新增注册用户数（用户增长曲线）
     * 5) 词库难度分布（饼图）
     * 6) 学习榜 TOP8（按累计学习单词数）
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totals", getSystemStatistics());

        LocalDate today = LocalDate.now();
        LocalDate start14 = today.minusDays(13);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        // 近14天每日学习行为（learning_statistics 每条 = 某用户某天有学习行为）
        List<LearningDailyStatistics> dailyRows = dailyStatisticsRepository.findByStatDateBetween(start14, today);
        Map<LocalDate, List<LearningDailyStatistics>> rowsByDate = dailyRows.stream()
                .collect(Collectors.groupingBy(LearningDailyStatistics::getStatDate));

        // 近14天活跃度序列
        List<Map<String, Object>> activity = new ArrayList<>();
        for (LocalDate d = start14; !d.isAfter(today); d = d.plusDays(1)) {
            List<LearningDailyStatistics> rows = rowsByDate.getOrDefault(d, Collections.emptyList());
            Set<Long> userIds = rows.stream()
                    .filter(r -> isActiveRow(r))
                    .map(r -> r.getUser().getId())
                    .collect(Collectors.toSet());
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.format(fmt));
            point.put("activeUsers", userIds.size());
            point.put("learned", rows.stream().mapToInt(r -> safe(r.getWordsLearned())).sum());
            point.put("reviewed", rows.stream().mapToInt(r -> safe(r.getWordsReviewed())).sum());
            activity.add(point);
        }
        overview.put("activity14", activity);

        // 今日打卡人数 = 今天有学习行为的用户数（学习即打卡）
        long todayActive = dailyStatisticsRepository.countActiveUsersOn(today);
        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("activeUsers", todayActive);
        todayStats.put("checkIns", todayActive);
        List<LearningDailyStatistics> todayRows = rowsByDate.getOrDefault(today, Collections.emptyList());
        todayStats.put("learned", todayRows.stream().mapToInt(r -> safe(r.getWordsLearned())).sum());
        todayStats.put("reviewed", todayRows.stream().mapToInt(r -> safe(r.getWordsReviewed())).sum());
        overview.put("today", todayStats);

        // 近14天新增用户（用户增长曲线）
        List<User> recentUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                        && !u.getCreatedAt().toLocalDate().isBefore(start14))
                .collect(Collectors.toList());
        Map<LocalDate, Long> signupByDate = recentUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getCreatedAt().toLocalDate(), Collectors.counting()));
        List<Map<String, Object>> userGrowth = new ArrayList<>();
        for (LocalDate d = start14; !d.isAfter(today); d = d.plusDays(1)) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.format(fmt));
            point.put("count", signupByDate.getOrDefault(d, 0L).intValue());
            userGrowth.add(point);
        }
        overview.put("userGrowth14", userGrowth);

        // 词库难度分布
        Map<String, Long> diffMap = new LinkedHashMap<>();
        diffMap.put("简单", 0L);
        diffMap.put("中等", 0L);
        diffMap.put("困难", 0L);
        for (Object[] row : wordRepository.countGroupByDifficulty()) {
            String level = String.valueOf(row[0]);
            Long count = ((Number) row[1]).longValue();
            if ("EASY".equals(level)) diffMap.put("简单", count);
            else if ("HARD".equals(level)) diffMap.put("困难", count);
            else diffMap.put("中等", count);
        }
        List<Map<String, Object>> difficultyDist = diffMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        overview.put("difficultyDist", difficultyDist);

        // 学习榜 TOP8
        overview.put("topUsers", buildTopUsers(8));

        // 最近公告（管理端看板可以顺带看到最新公告）
        return overview;
    }

    private boolean isActiveRow(LearningDailyStatistics r) {
        return safe(r.getWordsLearned()) > 0 || safe(r.getWordsReviewed()) > 0;
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }

    private List<Map<String, Object>> buildTopUsers(int limit) {
        List<User> users = userRepository.findAll();
        Map<Long, User> userById = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, Long> totalByUser = toCountMap(learningRecordRepository.countRecordsGroupByUser());
        Map<Long, Long> masteredByUser = toCountMap(
                learningRecordRepository.countRecordsGroupByUserAndStatus(LearningRecord.LearningStatus.MASTERED));

        List<Map<String, Object>> top = new ArrayList<>();
        totalByUser.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .forEach(e -> {
                    User u = userById.get(e.getKey());
                    if (u == null) return;
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("totalLearned", e.getValue());
                    m.put("mastered", masteredByUser.getOrDefault(e.getKey(), 0L));
                    top.add(m);
                });
        return top;
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            Number id = (Number) row[0];
            Number cnt = (Number) row[1];
            map.put(id.longValue(), cnt.longValue());
        }
        return map;
    }

    // ================== 用户学习详情（管理端） ==================

    /**
     * 管理端查看任意用户的学习详情：
     * 基础统计 + 学习记录（近100条）+ 未消灭的错词 + 收藏。
     * 返回的都是 Map 快照，避免把懒加载实体直接序列化。
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long totalRecords = learningRecordRepository.countTotalLearningWordsByUser(user);
        Long mastered = learningRecordRepository.countMasteredWordsByUser(user);
        int dueCount = learningRecordRepository.findDueReviews(user, LocalDateTime.now()).size();

        List<Favorite> favorites = favoriteRepository.findByUserWithWord(user);
        List<WrongWord> wrongWords = wrongWordRepository.findByUserAndResolvedWithWord(user, false);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", totalRecords);
        summary.put("mastered", mastered);
        summary.put("learning", Math.max(0, totalRecords - mastered));
        summary.put("dueReviews", dueCount);
        summary.put("favoriteCount", favorites.size());
        summary.put("wrongCount", wrongWords.size());

        // 学习记录快照（最新在前，最多 100 条）
        List<LearningRecord> records = learningRecordRepository.findByUserWithWord(user);
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (LearningRecord r : records.subList(0, Math.min(records.size(), 100))) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("status", r.getStatus());
            m.put("reviewCount", r.getReviewCount());
            m.put("correctCount", r.getCorrectCount());
            m.put("wrongCount", r.getWrongCount());
            m.put("nextReviewAt", r.getNextReviewAt());
            m.put("updatedAt", r.getUpdatedAt());
            if (r.getWord() != null) {
                Map<String, Object> w = new HashMap<>();
                w.put("id", r.getWord().getId());
                w.put("word", r.getWord().getWord());
                w.put("phonetic", r.getWord().getPhonetic());
                w.put("definition", r.getWord().getDefinition());
                m.put("word", w);
            }
            recordList.add(m);
        }

        // 错词快照
        List<Map<String, Object>> wrongList = wrongWords.stream()
                .map(w -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", w.getId());
                    m.put("wrongCount", w.getWrongCount());
                    m.put("lastWrongAt", w.getLastWrongAt());
                    if (w.getWord() != null) {
                        Map<String, Object> wordMap = new HashMap<>();
                        wordMap.put("id", w.getWord().getId());
                        wordMap.put("word", w.getWord().getWord());
                        wordMap.put("phonetic", w.getWord().getPhonetic());
                        wordMap.put("definition", w.getWord().getDefinition());
                        m.put("word", wordMap);
                    }
                    return m;
                })
                .collect(Collectors.toList());

        // 收藏快照
        List<Map<String, Object>> favoriteList = favorites.stream()
                .map(f -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", f.getId());
                    m.put("createdAt", f.getCreatedAt());
                    if (f.getWord() != null) {
                        Map<String, Object> wordMap = new HashMap<>();
                        wordMap.put("id", f.getWord().getId());
                        wordMap.put("word", f.getWord().getWord());
                        wordMap.put("phonetic", f.getWord().getPhonetic());
                        wordMap.put("definition", f.getWord().getDefinition());
                        m.put("word", wordMap);
                    }
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> detail = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("enabled", user.getEnabled());
        userMap.put("createdAt", user.getCreatedAt());
        detail.put("user", userMap);
        detail.put("summary", summary);
        detail.put("records", recordList);
        detail.put("wrongWords", wrongList);
        detail.put("favorites", favoriteList);
        return detail;
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
