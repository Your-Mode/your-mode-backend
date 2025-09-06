package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentQueryService {
    Page<ContentListResponseDto> getContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
    ContentDetailResponseDto getContentDetail(Integer contentId);
    
    // 내 컨텐츠 목록 조회 (특정 사용자가 요청한 ContentRequest에 대응하는 컨텐츠)
    Page<ContentListResponseDto> getMyContents(Integer userId, List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
    
    // 에디터 컨텐츠 목록 조회 (ContentRequest가 없는 컨텐츠 - 에디터가 자유롭게 작성)
    Page<ContentListResponseDto> getEditorContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
    
    // 맞춤형 컨텐츠 목록 조회 (ContentRequest가 있는 컨텐츠 - 사용자 요청에 의해 작성)
    Page<ContentListResponseDto> getCustomContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
    
    // 사용자가 댓글을 단 컨텐츠 목록 조회
    Page<ContentListResponseDto> getContentsByUserComments(Integer userId, List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
    
    // 사용자가 좋아요한 컨텐츠 목록 조회
    Page<ContentListResponseDto> getContentsByUserLikes(Integer userId, List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
}


