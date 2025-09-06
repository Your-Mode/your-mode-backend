package com.yourmode.yourmodebackend.domain.content.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CommentListResponseDto {
    private List<CommentResponseDto> comments;
    private Long totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private boolean hasNext;
}
