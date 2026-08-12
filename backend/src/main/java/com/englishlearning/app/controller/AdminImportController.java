package com.englishlearning.app.controller;

import com.englishlearning.app.dto.ImportWordItem;
import com.englishlearning.app.service.PdfImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 管理员：单词书导入。
 * 挂在 /admin/import 下，同样受 SecurityConfig 里 /admin/** -> ADMIN 角色限制。
 *
 * 两条解析路径：
 *   规则解析（正则，快、免费，排版要求高）：
 *     POST /admin/import/pdf        上传文件解析
 *     POST /admin/import/text       粘贴文本解析
 *   AI 智能解析（调用 DeepSeek 等，慢一些、需要 API Key、排版容错高）：
 *     POST /admin/import/ai/pdf     上传文件解析
 *     POST /admin/import/ai/text    粘贴文本解析
 *   最后统一确认导入：
 *     POST /admin/import/confirm
 */
@RestController
@RequestMapping("/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImportController {

    private final PdfImportService pdfImportService;

    public AdminImportController(PdfImportService pdfImportService) {
        this.pdfImportService = pdfImportService;
    }

    // ================== 规则解析 ==================

    @PostMapping("/pdf")
    public ResponseEntity<?> parseDocument(@RequestParam("file") MultipartFile file) {
        try {
            List<ImportWordItem> items = pdfImportService.parsePdf(file);
            return ResponseEntity.ok(items);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件解析失败：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/text")
    public ResponseEntity<?> parseText(@RequestBody Map<String, String> body) {
        try {
            List<ImportWordItem> items = pdfImportService.parseText(body.get("text"));
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ================== AI 智能解析 ==================

    @PostMapping("/ai/pdf")
    public ResponseEntity<?> parseDocumentWithAi(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = pdfImportService.parsePdfWithAi(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件解析失败：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/ai/text")
    public ResponseEntity<?> parseTextWithAi(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = pdfImportService.parseTextWithAi(body.get("text"));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ================== 确认导入 ==================

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmImport(@RequestBody List<ImportWordItem> items) {
        Map<String, Object> result = pdfImportService.confirmImport(items);
        return ResponseEntity.ok(result);
    }
}