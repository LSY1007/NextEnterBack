package org.zerock.nextenter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import java.io.File;

@Configuration
@Slf4j
// ✅ [경고 해결] Spring Data의 Page 객체를 JSON으로 변환할 때 안정적인 구조(PagedModel)를 사용하도록 강제함
// 이 설정이 없으면 "Direct serialization of PageImpl is not supported..." 경고가 계속 뜹니다.
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
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000") // React 포트 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}