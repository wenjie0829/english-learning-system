package com.englishlearning.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "word")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 100)
    private String phonetic;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(columnDefinition = "TEXT", name = "ai_definition")
    private String aiDefinition;

    @Column(length = 20)
    private String partOfSpeech;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    @Column(length = 500, name = "audio_url")
    private String audioUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 例句数量：不落库，只在查询时临时填充（管理端单词列表要显示"管理 (N)"）。
     * 之所以放在实体上而不是单独建 VO，是因为单词列表接口本来就返回 Word 实体，
     * 加一个 @Transient 字段改动面最小，也不会影响数据库表结构。
     */
    @Transient
    @EqualsAndHashCode.Exclude
    private Integer exampleCount;

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}
