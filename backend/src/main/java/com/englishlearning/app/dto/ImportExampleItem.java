package com.englishlearning.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量导入流程中，附属于某个单词的一条候选例句。
 * 只有 AI 智能解析这条路径会填充这个（正则规则解析不识别例句，字段留空）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportExampleItem {
    private String sentence;
    private String translation;
}