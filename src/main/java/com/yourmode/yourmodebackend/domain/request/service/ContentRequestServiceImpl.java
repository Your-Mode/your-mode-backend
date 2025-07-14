package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestStatusHistoryDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserProfileSummaryDto;
import com.yourmode.yourmodebackend.domain.request.entity.ContentRequest;
import com.yourmode.yourmodebackend.domain.request.entity.ContentRequestStatusHistory;
import com.yourmode.yourmodebackend.domain.request.entity.ItemCategory;
import com.yourmode.yourmodebackend.domain.request.entity.RequestStatusCode;
import com.yourmode.yourmodebackend.domain.request.repository.ContentRequestRepository;
import com.yourmode.yourmodebackend.domain.request.repository.ItemCategoryRepository;
import com.yourmode.yourmodebackend.domain.request.repository.RequestStatusCodeRepository;
import com.yourmode.yourmodebackend.domain.request.repository.ContentRequestStatusHistoryRepository;
import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.UserProfile;
import com.yourmode.yourmodebackend.domain.user.repository.UserProfileRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.repository.BodyTypeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentRequestServiceImpl implements ContentRequestService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BodyTypeRepository bodyTypeRepository;

    private final ContentRequestRepository contentRequestRepository;
    private final ContentRequestStatusHistoryRepository contentRequestStatusHistoryRepository;
    private final RequestStatusCodeRepository requestStatusCodeRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    @Override
    @Transactional
    public ContentRequestResponseDto createContentRequest(ContentRequestCreateDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        RequestStatusCode initialStatus = requestStatusCodeRepository.findByCodeName("신청 접수")
                .orElseThrow(() -> new IllegalArgumentException("Initial status not found"));

        Set<ItemCategory> itemCategories = new HashSet<>();
        if (dto.getItemCategoryIds() != null && !dto.getItemCategoryIds().isEmpty()) {
            itemCategories = new HashSet<>(itemCategoryRepository.findAllById(dto.getItemCategoryIds()));
        }

        ContentRequest contentRequest = ContentRequest.builder()
                .user(user)
                .bodyFeature(dto.getBodyFeature())
                .situation(dto.getSituation())
                .recommendedStyle(dto.getRecommendedStyle())
                .avoidedStyle(dto.getAvoidedStyle())
                .budget(dto.getBudget())
                .isPublic(dto.getIsPublic())
                .status(initialStatus)
                .itemCategories(itemCategories)
                .build();

        contentRequestRepository.save(contentRequest);

        // ✅ 상태 이력 기록 추가
        ContentRequestStatusHistory statusHistory = ContentRequestStatusHistory.builder()
                .contentRequest(contentRequest)
                .status(initialStatus)
                .editor(null) // 초기 생성이므로 에디터 없음
                .build();

        contentRequestStatusHistoryRepository.save(statusHistory);

        return ContentRequestResponseDto.builder()
                .id(contentRequest.getId())
                .situation(contentRequest.getSituation())
                .bodyFeature(contentRequest.getBodyFeature())
                .recommendedStyle(contentRequest.getRecommendedStyle())
                .avoidedStyle(contentRequest.getAvoidedStyle())
                .budget(contentRequest.getBudget())
                .isPublic(contentRequest.getIsPublic())
                .status(contentRequest.getStatus().getCodeName())
                .itemCategoryNames(contentRequest.getItemCategories().stream()
                        .map(ItemCategory::getName)
                        .collect(Collectors.toList()))
                .createdAt(contentRequest.getCreatedAt())
                .build();
    }


    @Override
    public List<ContentRequestSummaryDto> getRequestsByUserId(Long userId) {
        List<ContentRequest> requests = contentRequestRepository.findAllByUserId(userId);

        return requests.stream().map(request -> {
            List<ContentRequestStatusHistoryDto> historyDtos = request.getStatusHistories().stream()
                    .map(history -> ContentRequestStatusHistoryDto.builder()
                            .changedAt(history.getChangedAt())
                            .statusName(history.getStatus().getCodeName())
                            .editorId(history.getEditor() != null ? history.getEditor().getId() : null)
                            .editorName(history.getEditor() != null ? history.getEditor().getName() : null)
                            .build())
                    .collect(Collectors.toList());

            return com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestSummaryDto.builder()
                    .id(request.getId())
                    .bodyFeature(request.getBodyFeature())
                    .situation(request.getSituation())
                    .budget(request.getBudget())
                    .isPublic(request.getIsPublic())
                    .createdAt(request.getCreatedAt())
                    .statusHistories(historyDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    public ContentRequestDetailDto getContentRequestById(Integer id, Long userId) {
        ContentRequest request = contentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ContentRequest not found with id: " + id));

        List<Long> itemCategoryIds = request.getItemCategories().stream()
                .map(ItemCategory::getId)
                .collect(Collectors.toList());

        List<String> itemCategoryNames = request.getItemCategories().stream()
                .map(ItemCategory::getName)
                .collect(Collectors.toList());

        List<ContentRequestStatusHistoryDto> historyDtos = request.getStatusHistories().stream()
                .map(history -> ContentRequestStatusHistoryDto.builder()
                        .changedAt(history.getChangedAt())
                        .statusName(history.getStatus().getCodeName())
                        .editorId(history.getEditor() != null ? history.getEditor().getId() : null)
                        .editorName(history.getEditor() != null ? history.getEditor().getName() : null)
                        .build())
                .collect(Collectors.toList());

        UserProfile profileEntity = userProfileRepository.findByUserId(userId)
                .orElse(null);

        UserProfileSummaryDto profileDto = null;
        if (profileEntity != null) {
            String bodyTypeName = null;
            if (profileEntity.getBodyType() != null) {
                bodyTypeName = bodyTypeRepository.findById(profileEntity.getBodyType().getId())
                        .map(BodyType::getName)
                        .orElse(null);
            }
            profileDto = UserProfileSummaryDto.builder()
                    .name(request.getUser().getName())  // request에서 User 가져오기
                    .height(profileEntity.getHeight())
                    .weight(profileEntity.getWeight())
                    .bodyTypeName(bodyTypeName)
                    .build();
        }

        return ContentRequestDetailDto.builder()
                .id(request.getId())
                .bodyFeature(request.getBodyFeature())
                .situation(request.getSituation())
                .recommendedStyle(request.getRecommendedStyle())
                .avoidedStyle(request.getAvoidedStyle())
                .budget(request.getBudget())
                .isPublic(request.getIsPublic())
                .itemCategoryIds(itemCategoryIds)
                .itemCategoryNames(itemCategoryNames)
                .statusHistories(historyDtos)
                .profile(profileDto)
                .build();
    }


}
