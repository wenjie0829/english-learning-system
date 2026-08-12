package com.englishlearning.app.service;

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

    public AdminService(UserRepository userRepository, WordRepository wordRepository,
                         WordBookRepository wordBookRepository, ExampleSentenceRepository exampleSentenceRepository) {
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.wordBookRepository = wordBookRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
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
}