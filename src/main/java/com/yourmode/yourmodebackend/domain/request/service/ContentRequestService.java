package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestSummaryDto;

import java.util.List;

public interface ContentRequestService {

    ContentRequestResponseDto createContentRequest(ContentRequestCreateDto dto, Long userId);

    List<UserContentRequestSummaryDto> getRequestsByUserId(Long userId);

    UserContentRequestDetailDto getContentRequestById(Long id, Long userId);

    List<EditorContentRequestSummaryDto> getAllRequestsForEditor();

    EditorContentRequestDetailDto getContentRequestDetailForEditor(Long id);

    /**
     * 요청 상태 변경 및 상태 변경 이력 저장
     * @param requestId 변경할 요청 ID
     * @param statusCode 새 상태 코드
     * @param editorId 상태 변경자(에디터) ID
     */
    void updateStatus(Long requestId, String statusCode, Long editorId);
}
