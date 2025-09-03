package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentQueryService;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Contents: 조회 API", description = "Content 목록/상세 조회 API")
public class ContentQueryController {

    private final ContentQueryService contentQueryService;

    @GetMapping
    @Operation(
        summary = "전체 컨텐츠 목록 조회", 
        description = "전체 컨텐츠 목록을 조회합니다. 카테고리와 바디타입으로 필터링이 가능하며, 페이지네이션을 지원합니다."
    )
    public ResponseEntity<Page<ContentListResponseDto>> getContents(
            @Parameter(
                description = "카테고리 ID 목록 (예: 1,2,3). 여러 카테고리를 선택하면 OR 조건으로 검색됩니다. " +
                             "카테고리 목록: 스타일링 가이드(1), 트렌드 분석(2), 패션 팁(3), 시즌 컬렉션(4), 아이템 추천(5)",
                example = "1,2,3"
            )
            @RequestParam(required = false) List<Integer> categoryIds,
            
            @Parameter(
                description = "바디타입 ID 목록 (예: 1,2,3). 여러 바디타입을 선택하면 OR 조건으로 검색됩니다. " +
                             "바디타입 목록: 스트레이트(1), 웨이브(2), 내추럴(3), 선택 안함(4)",
                example = "1,3"
            )
            @RequestParam(required = false) List<Integer> bodyTypeIds,
            
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(contentQueryService.getContents(categoryIds, bodyTypeIds, pageable));
    }

    @GetMapping("/{contentId}")
    @Operation(summary = "컨텐츠 상세 조회", description = "특정 컨텐츠 상세 정보 조회")
    public ResponseEntity<ContentDetailResponseDto> getContentDetail(@PathVariable Integer contentId) {
        return ResponseEntity.ok(contentQueryService.getContentDetail(contentId));
    }

    @GetMapping("/my")
    @Operation(
        summary = "내 컨텐츠 목록 조회", 
        description = "현재 로그인한 사용자가 요청한 ContentRequest에 대응하는 컨텐츠 목록을 조회합니다. " +
                     "사용자가 스타일링 요청을 했고, 에디터가 작성한 컨텐츠들을 볼 수 있습니다."
    )
    public ResponseEntity<Page<ContentListResponseDto>> getMyContents(
            @Parameter(
                description = "카테고리 ID 목록 (예: 1,2,3). 여러 카테고리를 선택하면 OR 조건으로 검색됩니다. " +
                             "카테고리 목록: 스타일링 가이드(1), 트렌드 분석(2), 패션 팁(3), 시즌 컬렉션(4), 아이템 추천(5)",
                example = "1,2"
            )
            @RequestParam(required = false) List<Integer> categoryIds,
            
            @Parameter(
                description = "바디타입 ID 목록 (예: 1,2,3). 여러 바디타입을 선택하면 OR 조건으로 검색됩니다. " +
                             "바디타입 목록: 스트레이트(1), 웨이브(2), 내추럴(3), 선택 안함(4)",
                example = "1,3"
            )
            @RequestParam(required = false) List<Integer> bodyTypeIds,
            
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(contentQueryService.getMyContents(userDetails.getUserId(), categoryIds, bodyTypeIds, pageable));
    }

    @GetMapping("/editor")
    @Operation(
        summary = "에디터 컨텐츠 목록 조회", 
        description = "에디터가 자유롭게 작성한 컨텐츠 목록을 조회합니다. ContentRequest와 연결되지 않은 컨텐츠들입니다."
    )
    public ResponseEntity<Page<ContentListResponseDto>> getEditorContents(
            @Parameter(
                description = "카테고리 ID 목록 (예: 1,2,3). 여러 카테고리를 선택하면 OR 조건으로 검색됩니다. " +
                             "카테고리 목록: 스타일링 가이드(1), 트렌드 분석(2), 패션 팁(3), 시즌 컬렉션(4), 아이템 추천(5)",
                example = "1,2"
            )
            @RequestParam(required = false) List<Integer> categoryIds,
            
            @Parameter(
                description = "바디타입 ID 목록 (예: 1,2,3). 여러 바디타입을 선택하면 OR 조건으로 검색됩니다. " +
                             "바디타입 목록: 스트레이트(1), 웨이브(2), 내추럴(3), 선택 안함(4)",
                example = "1,3"
            )
            @RequestParam(required = false) List<Integer> bodyTypeIds,
            
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(contentQueryService.getEditorContents(categoryIds, bodyTypeIds, pageable));
    }

    @GetMapping("/custom")
    @Operation(
        summary = "맞춤형 컨텐츠 목록 조회", 
        description = "사용자 요청에 의해 작성된 맞춤형 컨텐츠 목록을 조회합니다. ContentRequest와 연결된 컨텐츠들입니다."
    )
    public ResponseEntity<Page<ContentListResponseDto>> getCustomContents(
            @Parameter(
                description = "카테고리 ID 목록 (예: 1,2,3). 여러 카테고리를 선택하면 OR 조건으로 검색됩니다. " +
                             "카테고리 목록: 스타일링 가이드(1), 트렌드 분석(2), 패션 팁(3), 시즌 컬렉션(4), 아이템 추천(5)",
                example = "1,2"
            )
            @RequestParam(required = false) List<Integer> categoryIds,
            
            @Parameter(
                description = "바디타입 ID 목록 (예: 1,2,3). 여러 바디타입을 선택하면 OR 조건으로 검색됩니다. " +
                             "바디타입 목록: 스트레이트(1), 웨이브(2), 내추럴(3), 선택 안함(4)",
                example = "1,3"
            )
            @RequestParam(required = false) List<Integer> bodyTypeIds,
            
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(contentQueryService.getCustomContents(categoryIds, bodyTypeIds, pageable));
    }
}


