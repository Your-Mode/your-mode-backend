package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestSummaryDto;
import java.util.List;

public interface EditorContentRequestService {
    List<EditorContentRequestSummaryDto> getAllRequestsForEditor();
    EditorContentRequestDetailDto getContentRequestDetailForEditor(Integer id);
    void updateStatus(Integer requestId, Integer statusId, Integer editorId);
} 