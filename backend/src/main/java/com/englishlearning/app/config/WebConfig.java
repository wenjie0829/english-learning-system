package com.englishlearning.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * 两件事：
 * 1) 只给 @RestController 加上 /api 前缀，静态资源（前端页面）不受影响，保持在根路径 / 下访问。
 * 2) 前端是单页应用（SPA）：/admin、/login、/learn 这些路径在服务器上并不真实存在，
 *    只有浏览器里点击链接跳转时，是 Vue Router 在前端接管、不发请求给后端。
 *    但如果用户直接输入网址或刷新页面，浏览器会真的向后端要这个路径，
 *    后端找不到对应文件就会 404/403。这里注册一个"找不到就兜底返回 index.html"的资源解析器，
 *    交还给前端 Vue Router 处理，从而支持任意子路径的直接访问/刷新。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api",
                c -> c.isAnnotationPresent(RestController.class));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable()
                                ? requestedResource
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}