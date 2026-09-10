package com.englishlearning.app.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 兜底配置：注册 Hibernate6Module 之后，任何"没有被主动 JOIN FETCH 加载"的懒加载关联字段，
 * Jackson 序列化时会自动处理成 null，而不是尝试访问已经关闭的数据库 Session 直接报错崩溃。
 *
 * 注意：这只是兜底安全网。真正需要在页面上展示的关联数据（比如错词本需要显示单词内容），
 * 还是要在 Repository 查询语句里用 JOIN FETCH 主动加载好，否则字段会变成 null（错词本看不到单词是什么）。
 * 具体见 WrongWordRepository / FavoriteRepository / LearningRecordRepository 里新增的 xxxWithWord 方法。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // 默认行为（USE_TRANSIENT_ANNOTATION=true）会把所有带 @Transient 的字段从 JSON 里直接剔除。
        // 但 @Transient 的语义只是"这个字段不落库"，不应该连接口返回也一起吞掉——
        // 单词列表里的 exampleCount（例句数量）就是这么凭空消失的，前端只能当成 0。
        // 项目里目前只有 Word.exampleCount 一个 @Transient 字段，关掉这个行为没有其他副作用。
        module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        return module;
    }
}