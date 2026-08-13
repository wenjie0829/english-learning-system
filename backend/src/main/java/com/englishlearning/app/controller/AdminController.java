package com.englishlearning.app.controller;

import com.englishlearning.app.entity.ExampleSentence;
import com.englishlearning.app.entity.User;
import com.englishlearning.app.entity.Word;
import com.englishlearning.app.security.UserPrincipal;
import com.englishlearning.app.service.AdminService;
import com.englishlearning.app.service.WordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员后台接口。
 * 路径全部挂在 /admin 下，在 SecurityConfig 中通过
 * requestMatchers("/admin/**").hasRole("ADMIN") 统一拦截，
 * 这里的 @PreAuthorize 是双保险，方法级别再校验一次角色。
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final WordService wordService;

    public AdminController(AdminService adminService, WordService wordService) {
        this.adminService = adminService;
        this.wordService = wordService;
    }

    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    // ================== 系统统计 ==================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        return ResponseEntity.ok(adminService.getSystemStatistics());
    }

    // ================== 用户管理 ==================

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam User.UserRole role) {
        User updated = adminService.updateUserRole(id, role);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        if (!enabled && id.equals(getCurrentAdminId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "不能封禁自己当前登录的账号"));
        }
        User updated = adminService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (id.equals(getCurrentAdminId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "不能删除自己当前登录的账号"));
        }
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // ================== 单词库管理 ==================

    @GetMapping("/words")
    public ResponseEntity<List<Word>> getAllWords() {
        return ResponseEntity.ok(wordService.getAllWords());
    }

    @PostMapping("/words")
    public ResponseEntity<Word> createWord(@RequestBody Word word) {
        return ResponseEntity.ok(wordService.createWord(word));
    }

    @PutMapping("/words/{id}")
    public ResponseEntity<Word> updateWord(@PathVariable Long id, @RequestBody Word word) {
        return ResponseEntity.ok(wordService.updateWord(id, word));
    }

    @DeleteMapping("/words/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/words/batch")
    public ResponseEntity<Map<String, Object>> deleteWords(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(wordService.deleteWords(ids));
    }
    
    @GetMapping("/words/{id}/examples")
    public ResponseEntity<List<ExampleSentence>> getExampleSentences(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getExampleSentences(id));
    }

    @PostMapping("/words/{id}/examples")
    public ResponseEntity<ExampleSentence> addExampleSentence(@PathVariable Long id, @RequestBody ExampleSentence exampleSentence) {
        return ResponseEntity.ok(wordService.addExampleSentence(id, exampleSentence));
    }

    @DeleteMapping("/words/examples/{exampleId}")
    public ResponseEntity<Void> deleteExampleSentence(@PathVariable Long exampleId) {
        wordService.deleteExampleSentence(exampleId);
        return ResponseEntity.ok().build();
    }
}