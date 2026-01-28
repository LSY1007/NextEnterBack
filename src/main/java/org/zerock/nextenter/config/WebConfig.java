package org.zerock.nextenter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import java.io.File;

@Configuration
@Slf4j
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
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

    // ❌ CORS 설정 제거 - SecurityConfig에서 관리
    // SecurityConfig와 WebConfig 둘 다 CORS를 설정하면 충돌 발생
    // SecurityConfig의 CORS 설정만 사용하도록 여기서는 제거
}