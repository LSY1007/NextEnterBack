package org.zerock.nextenter.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementDto {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String backgroundColor;
    private String buttonText;
    private String targetUrl;
    private String targetPage;
    private Boolean isActive;
    private Integer priority;
}
