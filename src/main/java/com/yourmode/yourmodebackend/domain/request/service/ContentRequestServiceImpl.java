package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.ContentRequestStatusHistoryDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.dto.UserProfileSummaryDto;
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
import java.util.Optional;

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
    public List<UserContentRequestSummaryDto> getRequestsByUserId(Long userId) {
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

            return UserContentRequestSummaryDto.builder()
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

    public UserContentRequestDetailDto getContentRequestById(Long id, Long userId) {
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

        return UserContentRequestDetailDto.builder()
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

    public EditorContentRequestDetailDto getContentRequestDetailForEditor(Long id) {
        // 1. 요청 데이터 조회 (User, UserProfile, ItemCategories, StatusHistories 포함)
        Optional<ContentRequest> optionalRequest = contentRequestRepository.findByIdWithUserAndProfile(id);
        if (optionalRequest.isEmpty()) {
            return null; // 또는 빈 DTO 반환
        }
        ContentRequest request = optionalRequest.get();

        // 2. UserProfile DTO 생성 (UserProfileSummaryDto)
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

        // 3. 상태 이력 리스트 DTO 생성
        List<ContentRequestStatusHistoryDto> historyDtos = request.getStatusHistories().stream()
                .map(history -> ContentRequestStatusHistoryDto.builder()
                        .changedAt(history.getChangedAt())
                        .statusName(history.getStatus().getCodeName())
                        .editorId(history.getEditor() != null ? history.getEditor().getId() : null)
                        .editorName(history.getEditor() != null ? history.getEditor().getName() : null)
                        .build())
                .collect(Collectors.toList());

        // 4. 카테고리 아이디, 이름 리스트 생성
        List<Long> categoryIds = request.getItemCategories().stream()
                .map(ItemCategory::getId)
                .collect(Collectors.toList());

        List<String> categoryNames = request.getItemCategories().stream()
                .map(ItemCategory::getName)
                .collect(Collectors.toList());

        // 5. DTO 생성 및 반환
        EditorContentRequestDetailDto dto = new EditorContentRequestDetailDto();
        dto.setId(request.getId());
        dto.setUser(request.getUser());
        dto.setProfile(profileDto);
        dto.setBodyFeature(request.getBodyFeature());
        dto.setSituation(request.getSituation());
        dto.setRecommendedStyle(request.getRecommendedStyle());
        dto.setAvoidedStyle(request.getAvoidedStyle());
        dto.setBudget(request.getBudget());
        dto.setIsPublic(request.getIsPublic());
        dto.setStatus(request.getStatus() != null ? request.getStatus().getCodeName() : null);
        dto.setItemCategoryIds(categoryIds);
        dto.setItemCategoryNames(categoryNames);
        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    @Override
    @Transactional
    public void updateStatus(Long requestId, String statusCode, Long editorId) {
        // 1. 요청 데이터 조회
        ContentRequest request = contentRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("컨텐츠 요청을 찾을 수 없습니다. id=" + requestId));

        // 2. 상태 코드 조회 (RequestStatusCode 엔티티)
        RequestStatusCode newStatus = requestStatusCodeRepository.findByCodeName(statusCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상태 코드입니다: " + statusCode));

        // 3. 에디터(사용자) 조회
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new IllegalArgumentException("에디터 사용자를 찾을 수 없습니다. id=" + editorId));

        // 4. 상태 변경
        request.setStatus(newStatus);
        contentRequestRepository.save(request);

        // 5. 상태 변경 이력 생성 및 저장
        ContentRequestStatusHistory history = ContentRequestStatusHistory.builder()
                .contentRequest(request)
                .status(newStatus)
                .editor(editor)
                .changedAt(LocalDateTime.now())
                .build();

        contentRequestStatusHistoryRepository.save(history);
    }
}
