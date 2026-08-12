package com.englishlearning.app.util;

import com.englishlearning.app.dto.ImportWordItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 PDF 提取出的纯文本里，尝试按行解析出"单词 - 音标 - 词性 - 释义"结构。
 *
 * PDF 单词书排版千差万别，这里用一套较宽松的正则尽量覆盖常见格式，例如：
 *   abandon /əˈbændən/ v. 放弃；抛弃
 *   abandon  v.抛弃
 *   1. abandon - 放弃
 *   abandon: 放弃
 *
 * 解析结果仅供预览，不保证 100% 准确，需要管理员在前端确认/修正后再入库。
 */
public class PdfWordParser {

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^\\s*(?:\\d{1,4}[.\\)、]\\s*)?" +                       // 可选的序号，如 "12. " "3) "
            "([A-Za-z][A-Za-z'\\-]{1,40})" +                          // 单词本身
            "\\s*(?:[\\[/]([^\\]/]{1,40})[\\]/])?" +                   // 可选音标 /.../ 或 [...]
            "\\s*[:\\-–—,，、]?\\s*" +
            "((?:n\\.|v\\.|vt\\.|vi\\.|adj\\.|adv\\.|prep\\.|conj\\.|pron\\.|int\\.|num\\.)?)\\s*" + // 可选词性缩写
            "(.+)$"                                                    // 剩余部分作为释义
    );

    private static final Pattern PURE_NUMBER = Pattern.compile("^\\s*\\d+\\s*$");

    private PdfWordParser() {
    }

    public static List<ImportWordItem> parse(String rawText) {
        Map<String, ImportWordItem> resultByWordLower = new LinkedHashMap<>();
        if (rawText == null || rawText.isBlank()) {
            return new ArrayList<>();
        }

        String[] lines = rawText.split("\\r?\\n");
        for (String line : lines) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || PURE_NUMBER.matcher(trimmed).matches()) {
                continue;
            }

            Matcher matcher = LINE_PATTERN.matcher(trimmed);
            if (!matcher.matches()) {
                continue;
            }

            String word = matcher.group(1);
            String phonetic = matcher.group(2);
            String pos = matcher.group(3);
            String definition = matcher.group(4);

            if (word == null || word.length() < 2) continue;
            if (definition == null || definition.trim().isEmpty()) continue;

            // 常见误命中过滤：全大写缩写、单词等于词性缩写本身等
            String wordLower = word.toLowerCase();
            if (resultByWordLower.containsKey(wordLower)) {
                // 同一个单词重复出现，跳过后面的，保留第一条
                continue;
            }

            ImportWordItem item = new ImportWordItem();
            item.setWord(word);
            item.setPhonetic(phonetic == null ? "" : phonetic.trim());
            item.setPartOfSpeech(pos == null ? "" : pos.trim());
            item.setDefinition(definition.trim());
            item.setAlreadyExists(false);

            resultByWordLower.put(wordLower, item);
        }

        return new ArrayList<>(resultByWordLower.values());
    }
}