package com.englishlearning.app.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EbbinghausUtil {

    // 艾宾浩斯遗忘曲线复习间隔（分钟）
    private static final int[] REVIEW_INTERVALS = {
        5,              // 5分钟后
        30,             // 30分钟后
        12 * 60,        // 12小时后
        24 * 60,        // 1天后
        2 * 24 * 60,    // 2天后
        4 * 24 * 60,    // 4天后
        7 * 24 * 60,    // 7天后
        15 * 24 * 60    // 15天后
    };

    /**
     * 计算下次复习时间
     * @param currentStage 当前艾宾浩斯阶段 (0-7)
     * @param baseTime 基准时间
     * @return 下次复习时间
     */
    public static LocalDateTime calculateNextReviewTime(int currentStage, LocalDateTime baseTime) {
        if (currentStage < 0 || currentStage >= REVIEW_INTERVALS.length) {
            // 超过阶段后，默认30天复习一次
            return baseTime.plusDays(30);
        }
        return baseTime.plus(REVIEW_INTERVALS[currentStage], ChronoUnit.MINUTES);
    }

    /**
     * 根据用户表现计算下一个阶段
     * @param currentStage 当前阶段
     * @param isCorrect 是否正确
     * @return 下一个阶段
     */
    public static int calculateNextStage(int currentStage, boolean isCorrect) {
        if (isCorrect) {
            // 答对，进入下一阶段
            return Math.min(currentStage + 1, REVIEW_INTERVALS.length - 1);
        } else {
            // 答错，重置到第一阶段
            return 0;
        }
    }

    /**
     * 判断是否应该复习
     * @param nextReviewTime 下次复习时间
     * @return 是否应该复习
     */
    public static boolean shouldReview(LocalDateTime nextReviewTime) {
        return LocalDateTime.now().isAfter(nextReviewTime) || LocalDateTime.now().isEqual(nextReviewTime);
    }

    /**
     * 获取当前阶段的描述
     * @param stage 阶段
     * @return 描述
     */
    public static String getStageDescription(int stage) {
        if (stage < 0 || stage >= REVIEW_INTERVALS.length) {
            return "已掌握";
        }
        
        int minutes = REVIEW_INTERVALS[stage];
        if (minutes < 60) {
            return minutes + "分钟后";
        } else if (minutes < 24 * 60) {
            return (minutes / 60) + "小时后";
        } else {
            return (minutes / (24 * 60)) + "天后";
        }
    }
}
