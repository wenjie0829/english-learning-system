package com.englishlearning.app.service;

import com.englishlearning.app.entity.*;
import com.englishlearning.app.repository.*;
import com.englishlearning.app.util.EbbinghausUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private final LearningRecordRepository learningRecordRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final WrongWordRepository wrongWordRepository;
    private final LearningDailyStatisticsRepository dailyStatisticsRepository;

    public LearningService(LearningRecordRepository learningRecordRepository, WordRepository wordRepository,
                          UserRepository userRepository, FavoriteRepository favoriteRepository,
                          WrongWordRepository wrongWordRepository,
                          LearningDailyStatisticsRepository dailyStatisticsRepository) {
        this.learningRecordRepository = learningRecordRepository;
        this.wordRepository = wordRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.dailyStatisticsRepository = dailyStatisticsRepository;
    }

    public List<LearningRecord> getUserLearningRecords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 用 JOIN FETCH 版本：这个结果要直接序列化成 JSON 返回给前端，
        // 必须把懒加载的 word 一起查出来，否则 open-in-view:false 下会报错
        return learningRecordRepository.findByUserWithWord(user);
    }

    public List<LearningRecord> getUserLearningRecordsByStatus(Long userId, LearningRecord.LearningStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return learningRecordRepository.findByUserAndStatusWithWord(user, status);
    }

    public List<LearningRecord> getDueReviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return learningRecordRepository.findDueReviewsWithWord(user, LocalDateTime.now());
    }

    @Transactional
    public LearningRecord startLearning(Long userId, Long wordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        Optional<LearningRecord> existingRecord = learningRecordRepository.findByUserAndWord(user, word);
        if (existingRecord.isPresent()) {
            return existingRecord.get();
        }

        LearningRecord record = new LearningRecord();
        record.setUser(user);
        record.setWord(word);
        record.setStatus(LearningRecord.LearningStatus.NEW);
        record.setReviewCount(0);
        record.setCorrectCount(0);
        record.setWrongCount(0);
        record.setEbbinghausStage(0);
        record.setNextReviewAt(LocalDateTime.now().plusMinutes(5)); // 首次5分钟后复习

        LearningRecord saved = learningRecordRepository.save(record);
        // 当天第一次学这个单词：计入"今日新学"
        recordDailyStat(user, 1, 0, 0, 0);
        return saved;
    }

    @Transactional
    public LearningRecord reviewWord(Long userId, Long wordId, boolean isCorrect) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        LearningRecord record = learningRecordRepository.findByUserAndWord(user, word)
                .orElseThrow(() -> new RuntimeException("Learning record not found"));

        record.setReviewCount(record.getReviewCount() + 1);
        record.setLastReviewAt(LocalDateTime.now());

        if (isCorrect) {
            record.setCorrectCount(record.getCorrectCount() + 1);
            record.setEbbinghausStage(EbbinghausUtil.calculateNextStage(record.getEbbinghausStage(), true));
            
            // 如果达到最高阶段，标记为已掌握
            if (record.getEbbinghausStage() >= 7) {
                record.setStatus(LearningRecord.LearningStatus.MASTERED);
            } else {
                record.setStatus(LearningRecord.LearningStatus.REVIEWING);
            }
        } else {
            record.setWrongCount(record.getWrongCount() + 1);
            record.setEbbinghausStage(EbbinghausUtil.calculateNextStage(record.getEbbinghausStage(), false));
            record.setStatus(LearningRecord.LearningStatus.LEARNING);
            
            // 添加到错词本
            addToWrongWordsDirect(user, word);
        }

        record.setNextReviewAt(EbbinghausUtil.calculateNextReviewTime(record.getEbbinghausStage(), LocalDateTime.now()));

        LearningRecord saved = learningRecordRepository.save(record);
        // 每次复习都计入"今日复习"，按对/错累计正确/错误数
        recordDailyStat(user, 0, 1, isCorrect ? 1 : 0, isCorrect ? 0 : 1);
        return saved;
    }

    /**
     * 累计当日学习统计（learning_statistics 表，按 user + 日期唯一）。
     * 当天只要学过或复习过单词，就会留下一条记录 —— 这就是"学习即打卡"的数据基础。
     */
    private void recordDailyStat(User user, int learned, int reviewed, int correct, int wrong) {
        LocalDate today = LocalDate.now();
        LearningDailyStatistics daily = dailyStatisticsRepository.findByUserAndStatDate(user, today)
                .orElseGet(() -> {
                    LearningDailyStatistics d = new LearningDailyStatistics();
                    d.setUser(user);
                    d.setStatDate(today);
                    d.setWordsLearned(0);
                    d.setWordsReviewed(0);
                    d.setWordsCorrect(0);
                    d.setWordsWrong(0);
                    d.setStudyDuration(0);
                    return d;
                });
        daily.setWordsLearned(daily.getWordsLearned() + learned);
        daily.setWordsReviewed(daily.getWordsReviewed() + reviewed);
        daily.setWordsCorrect(daily.getWordsCorrect() + correct);
        daily.setWordsWrong(daily.getWordsWrong() + wrong);
        dailyStatisticsRepository.save(daily);
    }

    // ================== 打卡 / 每日学习概览 ==================

    /**
     * 用户学习总览：基础统计 + 今日学习量 + 连续打卡天数 + 近30天日历。
     * 供首页打卡卡片与学习统计页一次性拉取。
     */
    public DailyOverview getDailyOverview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        LearningDailyStatistics todayStat = dailyStatisticsRepository.findByUserAndStatDate(user, today).orElse(null);

        List<LearningDailyStatistics> recentRows = dailyStatisticsRepository
                .findByUserAndStatDateBetween(user, today.minusDays(29), today);

        // 用于算连续天数的全量活跃日期（只看有学习/复习行为的记录）
        List<LearningDailyStatistics> allRows = dailyStatisticsRepository
                .findByUserAndStatDateGreaterThanEqual(user, today.minusDays(400));
        Set<LocalDate> activeDays = allRows.stream()
                .filter(r -> (r.getWordsLearned() != null && r.getWordsLearned() > 0)
                        || (r.getWordsReviewed() != null && r.getWordsReviewed() > 0))
                .map(LearningDailyStatistics::getStatDate)
                .collect(Collectors.toSet());

        // 连续打卡天数：今天已打卡就从今天往前数，今天还没打卡则从昨天往前数
        LocalDate cursor = activeDays.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        DailyOverview overview = new DailyOverview();
        overview.setTodayLearned(todayStat != null ? todayStat.getWordsLearned() : 0);
        overview.setTodayReviewed(todayStat != null ? todayStat.getWordsReviewed() : 0);
        overview.setTodayCorrect(todayStat != null ? todayStat.getWordsCorrect() : 0);
        overview.setTodayWrong(todayStat != null ? todayStat.getWordsWrong() : 0);
        overview.setCheckedInToday(todayStat != null
                && ((todayStat.getWordsLearned() != null && todayStat.getWordsLearned() > 0)
                || (todayStat.getWordsReviewed() != null && todayStat.getWordsReviewed() > 0)));
        overview.setStreakDays(streak);
        overview.setTotalCheckIns(activeDays.size());

        Map<LocalDate, LearningDailyStatistics> byDate = recentRows.stream()
                .collect(Collectors.toMap(LearningDailyStatistics::getStatDate, r -> r, (a, b) -> a));
        List<DailyEntry> calendar = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LearningDailyStatistics row = byDate.get(date);
            boolean active = row != null && ((row.getWordsLearned() != null && row.getWordsLearned() > 0)
                    || (row.getWordsReviewed() != null && row.getWordsReviewed() > 0));
            DailyEntry entry = new DailyEntry();
            entry.setDate(date);
            entry.setLearned(row != null ? row.getWordsLearned() : 0);
            entry.setReviewed(row != null ? row.getWordsReviewed() : 0);
            entry.setCheckedIn(active);
            calendar.add(entry);
        }
        overview.setCalendar(calendar);
        return overview;
    }

    /** 近30天日历条目 */
    public static class DailyEntry {
        private LocalDate date;
        private Integer learned;
        private Integer reviewed;
        private Boolean checkedIn;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public Integer getLearned() { return learned; }
        public void setLearned(Integer learned) { this.learned = learned; }
        public Integer getReviewed() { return reviewed; }
        public void setReviewed(Integer reviewed) { this.reviewed = reviewed; }
        public Boolean getCheckedIn() { return checkedIn; }
        public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    }

    /** 打卡总览 */
    public static class DailyOverview {
        private Integer todayLearned;
        private Integer todayReviewed;
        private Integer todayCorrect;
        private Integer todayWrong;
        private Boolean checkedInToday;
        private Integer streakDays;
        private Integer totalCheckIns;
        private List<DailyEntry> calendar = new ArrayList<>();

        public Integer getTodayLearned() { return todayLearned; }
        public void setTodayLearned(Integer todayLearned) { this.todayLearned = todayLearned; }
        public Integer getTodayReviewed() { return todayReviewed; }
        public void setTodayReviewed(Integer todayReviewed) { this.todayReviewed = todayReviewed; }
        public Integer getTodayCorrect() { return todayCorrect; }
        public void setTodayCorrect(Integer todayCorrect) { this.todayCorrect = todayCorrect; }
        public Integer getTodayWrong() { return todayWrong; }
        public void setTodayWrong(Integer todayWrong) { this.todayWrong = todayWrong; }
        public Boolean getCheckedInToday() { return checkedInToday; }
        public void setCheckedInToday(Boolean checkedInToday) { this.checkedInToday = checkedInToday; }
        public Integer getStreakDays() { return streakDays; }
        public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
        public Integer getTotalCheckIns() { return totalCheckIns; }
        public void setTotalCheckIns(Integer totalCheckIns) { this.totalCheckIns = totalCheckIns; }
        public List<DailyEntry> getCalendar() { return calendar; }
        public void setCalendar(List<DailyEntry> calendar) { this.calendar = calendar; }
    }

    @Transactional
    public void addToWrongWordsDirect(User user, Word word) {
        Optional<WrongWord> existingWrongWord = wrongWordRepository.findByUserAndWord(user, word);
        if (existingWrongWord.isPresent()) {
            WrongWord wrongWord = existingWrongWord.get();
            wrongWord.setWrongCount(wrongWord.getWrongCount() + 1);
            wrongWord.setLastWrongAt(LocalDateTime.now());
            wrongWord.setResolved(false);
            wrongWordRepository.save(wrongWord);
        } else {
            WrongWord wrongWord = new WrongWord();
            wrongWord.setUser(user);
            wrongWord.setWord(word);
            wrongWord.setWrongCount(1);
            wrongWord.setLastWrongAt(LocalDateTime.now());
            wrongWord.setResolved(false);
            wrongWordRepository.save(wrongWord);
        }
    }

    @Transactional
    public void addToFavorites(Long userId, Long wordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        if (!favoriteRepository.existsByUserAndWord(user, word)) {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setWord(word);
            favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFromFavorites(Long userId, Long wordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        favoriteRepository.findByUserAndWord(user, word).ifPresent(favoriteRepository::delete);
    }

    public List<Favorite> getUserFavorites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return favoriteRepository.findByUserWithWord(user);
    }

    @Transactional
    public void addToWrongWords(Long userId, Long wordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));
        addToWrongWordsDirect(user, word);
    }

    @Transactional
    public void markWrongWordAsResolved(Long userId, Long wordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        wrongWordRepository.findByUserAndWord(user, word).ifPresent(wrongWord -> {
            wrongWord.setResolved(true);
            wrongWord.setResolvedAt(LocalDateTime.now());
            wrongWordRepository.save(wrongWord);
        });
    }

    public List<WrongWord> getUserWrongWords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return wrongWordRepository.findByUserAndResolvedWithWord(user, false);
    }

    public LearningStatistics getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long totalWords = learningRecordRepository.countTotalLearningWordsByUser(user);
        Long masteredWords = learningRecordRepository.countMasteredWordsByUser(user);
        List<LearningRecord> dueReviews = learningRecordRepository.findDueReviews(user, LocalDateTime.now());

        LearningStatistics stats = new LearningStatistics();
        stats.setTotalWords(totalWords.intValue());
        stats.setMasteredWords(masteredWords.intValue());
        stats.setLearningWords(totalWords.intValue() - masteredWords.intValue());
        stats.setDueReviews(dueReviews.size());

        return stats;
    }

    public static class LearningStatistics {
        private Integer totalWords;
        private Integer masteredWords;
        private Integer learningWords;
        private Integer dueReviews;

        public Integer getTotalWords() { return totalWords; }
        public void setTotalWords(Integer totalWords) { this.totalWords = totalWords; }
        public Integer getMasteredWords() { return masteredWords; }
        public void setMasteredWords(Integer masteredWords) { this.masteredWords = masteredWords; }
        public Integer getLearningWords() { return learningWords; }
        public void setLearningWords(Integer learningWords) { this.learningWords = learningWords; }
        public Integer getDueReviews() { return dueReviews; }
        public void setDueReviews(Integer dueReviews) { this.dueReviews = dueReviews; }
    }
}