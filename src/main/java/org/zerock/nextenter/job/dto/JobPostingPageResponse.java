package org.zerock.nextenter.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingPageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<JobPostingListResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
