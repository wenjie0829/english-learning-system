package com.englishlearning.app.service;

import com.englishlearning.app.entity.*;
import com.englishlearning.app.repository.*;
import com.englishlearning.app.util.EbbinghausUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LearningService {

    private final LearningRecordRepository learningRecordRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final WrongWordRepository wrongWordRepository;

    public LearningService(LearningRecordRepository learningRecordRepository, WordRepository wordRepository,
                          UserRepository userRepository, FavoriteRepository favoriteRepository,
                          WrongWordRepository wrongWordRepository) {
        this.learningRecordRepository = learningRecordRepository;
        this.wordRepository = wordRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.wrongWordRepository = wrongWordRepository;
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

        return learningRecordRepository.save(record);
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

        return learningRecordRepository.save(record);
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