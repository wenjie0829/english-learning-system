package com.englishlearning.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
// 改成 "users"：user 在 PostgreSQL 里是保留关键字，容易踩坑，换个安全的表名
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // 密码哈希绝不能出现在任何接口返回的 JSON 里
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.STUDENT;

    // 账号是否启用；管理员可以通过后台封禁/解封用户
    // 去掉了 MySQL 专属的 TINYINT(1)，PostgreSQL 用 boolean。
    // Java 字段已经有默认值 true，插入新数据时够用了，不再需要写死 columnDefinition。
    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UserRole {
        STUDENT, ADMIN
    }
}