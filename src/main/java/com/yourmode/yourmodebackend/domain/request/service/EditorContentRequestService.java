package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestSummaryDto;
import java.util.List;

public interface EditorContentRequestService {
    List<EditorContentRequestSummaryDto> getAllRequestsForEditor();
    EditorContentRequestDetailDto getContentRequestDetailForEditor(Long id);
    void updateStatus(Long requestId, String statusCode, Long editorId);
} 