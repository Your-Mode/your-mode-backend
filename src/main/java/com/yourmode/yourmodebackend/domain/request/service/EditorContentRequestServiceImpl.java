package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.dto.UserProfileSummaryDto;
import com.yourmode.yourmodebackend.domain.request.dto.ContentRequestStatusHistoryDto;
import com.yourmode.yourmodebackend.domain.request.entity.ContentRequest;
import com.yourmode.yourmodebackend.domain.request.entity.ContentRequestStatusHistory;
import com.yourmode.yourmodebackend.domain.request.entity.ItemCategory;
import com.yourmode.yourmodebackend.domain.request.entity.RequestStatusCode;
import com.yourmode.yourmodebackend.domain.request.repository.ContentRequestRepository;
import com.yourmode.yourmodebackend.domain.request.repository.RequestStatusCodeRepository;
import com.yourmode.yourmodebackend.domain.request.repository.ContentRequestStatusHistoryRepository;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.domain.request.status.RequestErrorStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EditorContentRequestServiceImpl implements EditorContentRequestService {
    private final ContentRequestRepository contentRequestRepository;
    private final ContentRequestStatusHistoryRepository contentRequestStatusHistoryRepository;
    private final RequestStatusCodeRepository requestStatusCodeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public List<EditorContentRequestSummaryDto> getAllRequestsForEditor() {
        List<ContentRequest> requests = contentRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().map(request -> {
            List<ContentRequestStatusHistoryDto> historyDtos = request.getStatusHistories().stream()
                    .map(history -> ContentRequestStatusHistoryDto.builder()
                            .changedAt(history.getChangedAt())
                            .statusName(history.getStatus().getCodeName())
                            .editorId(history.getEditor() != null ? history.getEditor().getId() : null)
                            .editorName(history.getEditor() != null ? history.getEditor().getName() : null)
                            .build())
                    .collect(Collectors.toList());
            List<Long> categoryIds = request.getItemCategories().stream()
                    .map(ItemCategory::getId)
                    .collect(Collectors.toList());
            List<String> categoryNames = request.getItemCategories().stream()
                    .map(ItemCategory::getName)
                    .collect(Collectors.toList());
            UserProfileSummaryDto profileDto = userProfileRepository.findByUserId(request.getUser().getId())
                    .map(profile -> UserProfileSummaryDto.builder()
                            .name(request.getUser().getName())
                            .height(profile.getHeight())
                            .weight(profile.getWeight())
                            .bodyTypeName(profile.getBodyType() != null ? profile.getBodyType().getName() : null)
                            .build())
                    .orElse(null);
            return EditorContentRequestSummaryDto.builder()
                    .id(request.getId())
                    .profile(profileDto)
                    .bodyFeature(request.getBodyFeature())
                    .situation(request.getSituation())
                    .recommendedStyle(request.getRecommendedStyle())
                    .createdAt(request.getCreatedAt())
                    .itemCategoryIds(categoryIds)
                    .itemCategoryNames(categoryNames)
                    .statusHistories(historyDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public EditorContentRequestDetailDto getContentRequestDetailForEditor(Long id) {
        Optional<ContentRequest> optionalRequest = contentRequestRepository.findByIdWithUserAndProfile(id);
        if (optionalRequest.isEmpty()) {
            return null;
        }
        ContentRequest request = optionalRequest.get();
        UserProfileSummaryDto profileDto = null;
        if (request.getUser().getProfile() != null) {
            profileDto = UserProfileSummaryDto.builder()
                    .name(request.getUser().getName())
                    .height(request.getUser().getProfile().getHeight())
                    .weight(request.getUser().getProfile().getWeight())
                    .bodyTypeName(request.getUser().getProfile().getBodyType() != null
                            ? request.getUser().getProfile().getBodyType().getName() : null)
                    .build();
        }
        List<ContentRequestStatusHistoryDto> historyDtos = request.getStatusHistories().stream()
                .map(history -> ContentRequestStatusHistoryDto.builder()
                        .changedAt(history.getChangedAt())
                        .statusName(history.getStatus().getCodeName())
                        .editorId(history.getEditor() != null ? history.getEditor().getId() : null)
                        .editorName(history.getEditor() != null ? history.getEditor().getName() : null)
                        .build())
                .collect(Collectors.toList());
        List<Long> categoryIds = request.getItemCategories().stream()
                .map(ItemCategory::getId)
                .collect(Collectors.toList());
        List<String> categoryNames = request.getItemCategories().stream()
                .map(ItemCategory::getName)
                .collect(Collectors.toList());
        EditorContentRequestDetailDto dto = EditorContentRequestDetailDto.builder()
                .id(request.getId())
                .profile(profileDto)
                .bodyFeature(request.getBodyFeature())
                .situation(request.getSituation())
                .recommendedStyle(request.getRecommendedStyle())
                .avoidedStyle(request.getAvoidedStyle())
                .budget(request.getBudget())
                .isPublic(request.getIsPublic())
                .status(request.getStatus() != null ? request.getStatus().getCodeName() : null)
                .itemCategoryIds(categoryIds)
                .itemCategoryNames(categoryNames)
                .createdAt(request.getCreatedAt())
                .build();
        return dto;
    }

    @Override
    @Transactional
    public void updateStatus(Long requestId, String statusCode, Long editorId) {
        ContentRequest request = contentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RestApiException(RequestErrorStatus.REQUEST_NOT_FOUND));
        RequestStatusCode newStatus = requestStatusCodeRepository.findByCodeName(statusCode)
                .orElseThrow(() -> new RestApiException(RequestErrorStatus.INVALID_REQUEST_STATUS));
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new RestApiException(RequestErrorStatus.REQUEST_NOT_FOUND));
        request.setStatus(newStatus);
        contentRequestRepository.save(request);
        ContentRequestStatusHistory history = ContentRequestStatusHistory.builder()
                .contentRequest(request)
                .status(newStatus)
                .editor(editor)
                .changedAt(LocalDateTime.now())
                .build();
        contentRequestStatusHistoryRepository.save(history);
    }
} 