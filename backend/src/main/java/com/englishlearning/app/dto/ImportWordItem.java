package com.englishlearning.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * PDF/文本/AI 导入流程中的一条单词候选记录。
 * 解析出来只是预览，不入库；管理员在前端确认（可能已编辑）之后，
 * 通过 /admin/import/confirm 才真正写入数据库，同时把 examples 里的例句一并写入。
 *
 * 注意：这里手写了无参构造器和 getter/setter（没有用 @AllArgsConstructor），
 * 是为了让 examples 字段有一个非 null 的默认值（空列表），避免 Jackson 反序列化时
 * 因为 AI 返回结果没带 examples 字段而导致 NPE。
 */
@Data
@NoArgsConstructor
public class ImportWordItem {
    private String word;
    private String phonetic;
    private String partOfSpeech;
    private String definition;
    // 解析预览阶段：标记数据库里是否已经存在同名单词，供前端提示
    private boolean alreadyExists;
    // 配套例句，只有 AI 智能解析会填充；规则解析（正则）路径这里始终是空列表
    private List<ImportExampleItem> examples = new ArrayList<>();
}