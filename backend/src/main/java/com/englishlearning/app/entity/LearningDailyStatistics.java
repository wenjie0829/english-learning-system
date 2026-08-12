package com.englishlearning.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningDailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "words_learned")
    private Integer wordsLearned = 0;

    @Column(name = "words_reviewed")
    private Integer wordsReviewed = 0;

    @Column(name = "words_correct")
    private Integer wordsCorrect = 0;

    @Column(name = "words_wrong")
    private Integer wordsWrong = 0;

    @Column(name = "study_duration")
    private Integer studyDuration = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
