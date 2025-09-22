package com.yourmode.yourmodebackend.domain.survey.controller;

import com.yourmode.yourmodebackend.domain.survey.dto.request.SurveyAnswersSubmitRequestDto;
import com.yourmode.yourmodebackend.domain.survey.dto.request.SurveyTextAnswersRequestDto;
import com.yourmode.yourmodebackend.domain.survey.dto.response.*;
import com.yourmode.yourmodebackend.domain.survey.service.SurveyService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey: 골격 진단 설문 API", description = "질문/답변/이력 관련 API")
public class SurveyController {
    private final SurveyService surveyService;

    @Operation(
        summary = "설문 질문+옵션 전체 조회", 
        description = "골격 진단을 위한 모든 설문 질문과 각 질문의 선택 옵션을 조회합니다. 질문은 순서대로 정렬되어 반환됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    summary = "질문+옵션 목록 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": [
                            {
                                "questionId": 1,
                                "questionContent": "당신의 체형은 어떤 느낌인가요?",
                                "options": [
                                    {
                                        "optionId": 1,
                                        "optionContent": "두께감이 있고 육감적이다"
                                    },
                                    {
                                        "optionId": 2,
                                        "optionContent": "날씬하고 가벼운 느낌이다"
                                    }
                                ]
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "설문 문항 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "설문 문항 없음",
                    summary = "설문 문항을 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-002",
                        "message": "설문 문항을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    summary = "DB 조회 중 오류 발생",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-500-001",
                        "message": "설문 질문 조회 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/questions")
    public ResponseEntity<BaseResponse<List<SurveyQuestionWithOptionsResponseDto>>> getAllQuestionsWithOptions() {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getAllQuestionsWithOptions()));
    }

    @Operation(
        summary = "설문 답변 일괄 저장", 
        description = "사용자가 선택한 모든 설문 답변을 한 번에 저장합니다. 설문 이력이 생성되고 각 답변은 해당 이력과 연결됩니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "설문 답변 일괄 저장 요청",
        required = true,
        content = @Content(
            schema = @Schema(implementation = SurveyAnswersSubmitRequestDto.class),
            examples = @ExampleObject(
                name = "설문 답변 15개 예시",
                summary = "골격 진단 설문 답변",
                value = """
                {
                  "answers": [
                    {"questionId": 1, "optionId": 1},
                    {"questionId": 2, "optionId": 3},
                    {"questionId": 3, "optionId": 5},
                    {"questionId": 4, "optionId": 7},
                    {"questionId": 5, "optionId": 9},
                    {"questionId": 6, "optionId": 11},
                    {"questionId": 7, "optionId": 13},
                    {"questionId": 8, "optionId": 15},
                    {"questionId": 9, "optionId": 17},
                    {"questionId": 10, "optionId": 19},
                    {"questionId": 11, "optionId": 21},
                    {"questionId": 12, "optionId": 23},
                    {"questionId": 13, "optionId": 25},
                    {"questionId": 14, "optionId": 27},
                    {"questionId": 15, "optionId": 29}
                  ]
                }
                """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "저장 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "저장 성공 예시",
                    summary = "설문 답변 저장 성공",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "historyId": 1,
                            "answerCount": 15
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = {
                    @ExampleObject(
                        name = "답변 목록 누락",
                        summary = "answers 필드가 null이거나 비어있는 경우",
                        value = """
                        {
                            "timestamp": "2025-07-17T12:00:00.000",
                            "code": "SURVEY-400-006",
                            "message": "답변 목록이 비어있습니다."
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "유효하지 않은 사용자 ID",
                        summary = "userId가 null이거나 유효하지 않은 경우",
                        value = """
                        {
                            "timestamp": "2025-07-17T12:00:00.000",
                            "code": "SURVEY-400-001",
                            "message": "유효하지 않은 사용자 ID입니다."
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "유효하지 않은 질문 ID",
                        summary = "questionId가 존재하지 않는 경우",
                        value = """
                        {
                            "timestamp": "2025-07-17T12:00:00.000",
                            "code": "SURVEY-400-002",
                            "message": "유효하지 않은 질문 ID입니다."
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "유효하지 않은 옵션 ID",
                        summary = "optionId가 존재하지 않는 경우",
                        value = """
                        {
                            "timestamp": "2025-07-17T12:00:00.000",
                            "code": "SURVEY-400-003",
                            "message": "유효하지 않은 옵션 ID입니다."
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    summary = "DB 저장 중 오류 발생",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-500-101",
                        "message": "설문 답변을 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/answers")
    public ResponseEntity<BaseResponse<SurveySaveResponseDto>> saveSurveyAnswersBulk(
            @RequestBody @Valid SurveyAnswersSubmitRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.saveSurveyAnswersBulk(dto, userDetails.getUserId())));
    }

    @Operation(
        summary = "내 설문 이력+답변 전체 조회", 
        description = "사용자의 모든 설문 이력과 각 이력의 상세 답변을 조회합니다. 최신 이력부터 정렬되어 반환됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    summary = "설문 이력+답변 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": [
                            {
                                "historyId": 1,
                                "createdAt": "2025-07-17T12:00:00.000",
                                "answers": [
                                    {
                                        "questionId": 1,
                                        "questionContent": "...",
                                        "optionId": 1,
                                        "optionContent": "..."
                                    }
                                ]
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "설문 이력 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "설문 이력 없음",
                    summary = "설문 이력을 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-001",
                        "message": "설문을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    summary = "DB 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-500-001",
                        "message": "설문 정보를 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/histories/me")
    public ResponseEntity<BaseResponse<List<SurveyHistoryWithAnswersResponseDto>>> getMySurveyHistoriesWithAnswers(@CurrentUser PrincipalDetails userDetails) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getSurveyHistoriesWithAnswers(userDetails.getUserId())));
    }

    @Operation(
        summary = "설문 답변 목록 조회", 
        description = "특정 설문 이력의 모든 답변을 조회합니다. 질문과 선택한 옵션 정보가 포함됩니다."
    )
    @Parameter(
        name = "historyId", 
        description = "조회할 설문 이력 ID", 
        required = true,
        example = "1"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    summary = "답변 목록 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": [
                            {
                                "questionId": 1,
                                "questionContent": "...",
                                "optionId": 1,
                                "optionContent": "..."
                            },
                            {
                                "questionId": 2,
                                "questionContent": "...",
                                "optionId": 3,
                                "optionContent": "..."
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "접근 권한 없음",
                    summary = "다른 사용자의 설문 이력에 접근하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-403-001",
                        "message": "해당 설문에 대한 접근 권한이 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "답변 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "답변 없음",
                    summary = "해당 설문 이력에 답변이 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-003",
                        "message": "설문 답변을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    summary = "DB 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-500-002",
                        "message": "설문 답변 조회 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/histories/{historyId}/answers")
    public ResponseEntity<BaseResponse<List<SurveyAnswerResponseDto>>> getSurveyAnswersByHistory(
            @PathVariable Integer historyId,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getSurveyAnswersByHistory(historyId, userDetails.getUserId())));
    }

    @Operation(
        summary = "저장된 설문 이력 분석", 
        description = "이미 저장된 설문 이력의 답변을 FastAPI로 분석하여 체형 진단 결과를 반환합니다. 분석 결과는 자동으로 DB에 저장됩니다."
    )
    @Parameter(
        name = "historyId", 
        description = "분석할 설문 이력 ID", 
        required = true,
        example = "1"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "분석 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "분석 성공 예시",
                    summary = "FastAPI 분석 결과 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "bodyType": "스트레이트형",
                            "typeDescription": "탄탄하고 직선적인 느낌의 체형입니다.",
                            "detailedFeatures": "근육이 잘 붙고, 어깨가 넓은 편입니다.",
                            "attractionPoints": "탄탄한 상체",
                            "recommendedStyles": "슬림핏, 미니멀룩",
                            "avoidStyles": "오버핏",
                            "stylingFixes": "허리 라인을 강조",
                            "stylingTips": "밝은 컬러 활용"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "접근 권한 없음",
                    summary = "다른 사용자의 설문에 접근하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-403-001",
                        "message": "해당 설문에 대한 접근 권한이 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "설문 이력 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "설문 이력 없음",
                    summary = "해당 ID의 설문 이력을 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-001",
                        "message": "설문을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "502",
            description = "체형 분석 서비스 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = {
                    @ExampleObject(
                        name = "체형 분석 서비스 연결 실패",
                        summary = "체형 분석 서비스와 통신 실패",
                        value = """
                        {
                            "timestamp": "2025-07-17T12:00:00.000",
                            "code": "SURVEY-502-001",
                            "message": "체형 분석 서비스 호출에 실패했습니다."
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "체형 분석 서비스 응답 오류",
                        summary = "체형 분석 서비스에서 유효하지 않은 응답",
                        value = """
                        {
                            "timestamp": "2025-07-17T12:00:00.000",
                            "code": "SURVEY-502-002",
                            "message": "체형 분석 서비스에서 유효하지 않은 응답을 받았습니다."
                        }
                        """
                    )
                }
            )
        )
    })
    @PostMapping("/histories/{historyId}/analysis")
    public ResponseEntity<BaseResponse<SurveyResultFastApiResponseDto>> analyzeSurveyHistoryWithFast(
            @PathVariable Integer historyId,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.analyzeSurveyHistoryWithFast(historyId, userDetails.getUserId())));
    }

    @Operation(
        summary = "직접 답변 체형 분석", 
        description = "사용자가 직접 입력한 텍스트 답변과 신체정보를 FastAPI로 분석하여 체형 진단 결과를 반환합니다. 분석 결과는 자동으로 DB에 저장됩니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "체형 분석 요청 데이터",
        required = true,
        content = @Content(
            schema = @Schema(implementation = SurveyTextAnswersRequestDto.class),
            examples = {
                @ExampleObject(
                    name = "체형 분석 요청 예시",
                    summary = "사용자의 설문 답변과 신체정보",
                    value = """
                    {
                      "answers": [
                        "두께감이 있고 육감적이다",
                        "피부가 탄탄하고 쫀득한 편이다",
                        "근육이 붙기 쉽다",
                        "목이 약간 짧은 편이다",
                        "허리가 짧고 직선적인 느낌이며 굴곡이 적다",
                        "두께감이 있고, 바스트 탑의 위치가 높다",
                        "어깨가 넓고 직선적인 느낌이며, 탄탄한 인상을 준다",
                        "엉덩이 라인의 위쪽부터 볼륨감이 있으며 탄력있다",
                        "허벅지가 단단하고 근육이 많아 탄력이 있다",
                        "손이 작고 손바닥에 두께감이 있다",
                        "손목이 가늘고 둥근 편이다",
                        "발이 작고 발목이 가늘며 단단하다",
                        "무릎이 작고 부각되지 않는 편이다",
                        "쇄골이 거의 보이지 않는다",
                        "둥근 얼굴이며, 볼이 통통한 편이다",
                        "상체가 발달한 느낌이며 허리가 짧고 탄탄한 인상을 준다",
                        "팔, 가슴, 배 등 상체 위주로 찐다"
                      ],
                      "gender": "여성",
                      "height": 164.5,
                      "weight": 55.2
                    }
                    """
                )
            }
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "분석 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "분석 성공 예시",
                    summary = "FastAPI 분석 결과 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "bodyType": "스트레이트형",
                            "typeDescription": "탄탄하고 직선적인 느낌의 체형입니다.",
                            "detailedFeatures": "근육이 잘 붙고, 어깨가 넓은 편입니다.",
                            "attractionPoints": "탄탄한 상체",
                            "recommendedStyles": "슬림핏, 미니멀룩",
                            "avoidStyles": "오버핏",
                            "stylingFixes": "허리 라인을 강조",
                            "stylingTips": "밝은 컬러 활용"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    summary = "요청 데이터가 유효하지 않은 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-400-007",
                        "message": "체형 분석 요청 데이터가 유효하지 않습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "502",
            description = "체형 분석 서비스 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "체형 분석 서비스 오류",
                    summary = "체형 분석 서비스와 통신 실패",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-502-001",
                        "message": "체형 분석 서비스 호출에 실패했습니다."
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/analysis")
    public ResponseEntity<BaseResponse<SurveyResultFastApiResponseDto>> analyzeSurveyAnswersWithFast(
            @RequestBody SurveyTextAnswersRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.analyzeSurveyAnswersWithFast(dto, userDetails.getUserId())));
    }

    @Operation(
        summary = "내 설문 결과 목록 조회", 
        description = "사용자의 모든 체형 진단 결과 목록을 조회합니다. 최신 결과부터 정렬되어 반환됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    summary = "설문 결과 목록 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": [
                            {
                                "resultId": 1,
                                "bodyTypeName": "내추럴",
                                "historyId": 1,
                                "createdAt": "2025-07-17T12:00:00.000"
                            },
                            {
                                "resultId": 2,
                                "bodyTypeName": "스트레이트",
                                "historyId": 2,
                                "createdAt": "2025-07-16T10:30:00.000"
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "설문 결과 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "설문 결과 없음",
                    summary = "설문 결과를 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-004",
                        "message": "설문 결과를 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    summary = "DB 조회 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-500-004",
                        "message": "설문 결과 조회 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/results/me")
    public ResponseEntity<BaseResponse<List<SurveyResultSummaryDto>>> getMySurveyResults(@CurrentUser PrincipalDetails userDetails) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getSurveyResultsByUserId(userDetails.getUserId())));
    }

    @Operation(
        summary = "설문 결과 상세 조회", 
        description = "특정 체형 진단 결과의 상세 내용을 조회합니다. 체형 타입, 특징, 스타일링 팁 등이 포함됩니다."
    )
    @Parameter(
        name = "resultId", 
        description = "조회할 설문 결과 ID", 
        required = true,
        example = "1"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    summary = "설문 결과 상세 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "resultId": 1,
                            "bodyTypeName": "내추럴",
                            "typeDescription": "당신의 체형은 내추럴 타입으로 진단됩니다...",
                            "detailedFeatures": "당신의 신체는 두께감 있고 육감적인 면이 강하게 드러납니다...",
                            "attractionPoints": "내추럴 체형의 가장 큰 매력 포인트는 바로 건강하고 탄력 있는 몸매입니다...",
                            "recommendedStyles": "내추럴 체형에는 레이어링과 구조적인 디자인이 잘 어울립니다...",
                            "avoidStyles": "내추럴 체형은 지나치게 부드럽고 흐트러진 실루엣의 의상은 피하는 것이 좋습니다...",
                            "stylingFixes": "내추럴 체형의 장점을 최대한 살리기 위해서는 레이어링을 활용하여 전체적인 균형을 잡는 것이 중요합니다...",
                            "stylingTips": "내추럴 체형은 다양한 스타일을 소화할 수 있는 장점이 있습니다...",
                            "historyId": 1,
                            "createdAt": "2025-07-17T12:00:00.000"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "설문 결과 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "설문 결과 없음",
                    summary = "설문 결과를 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-004",
                        "message": "설문 결과를 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "서버 오류",
                    summary = "DB 조회 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-500-004",
                        "message": "설문 결과 조회 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/results/{resultId}")
    public ResponseEntity<BaseResponse<SurveyResultResponseDto>> getSurveyResultDetail(
            @PathVariable Integer resultId,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getSurveyResultDetail(resultId, userDetails.getUserId())));
    }
} 