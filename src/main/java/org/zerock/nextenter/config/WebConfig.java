package org.zerock.nextenter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads 폴더 전체 매핑
        File uploadDir = new File(uploadBaseDir);
        String absolutePath = "file:" + uploadDir.getAbsolutePath() + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);

        log.info("정적 리소스 경로 설정: /uploads/** -> {}", absolutePath);
    }
}