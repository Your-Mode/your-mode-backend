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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey: 설문 API", description = "설문 질문/답변/이력 관련 API")
public class SurveyController {
    private final SurveyService surveyService;

    @Operation(summary = "설문 질문+옵션 전체 조회", description = "모든 설문 질문과 각 질문의 옵션(키값 포함)을 반환합니다.")
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
                        "result": [ /* ... */ ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문항 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "문항 없음",
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
                    summary = "서버 내부 오류 발생",
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
    @GetMapping("/questions")
    public ResponseEntity<BaseResponse<List<SurveyQuestionWithOptionsResponseDto>>> getAllQuestionsWithOptions() {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getAllQuestionsWithOptions()));
    }

    @Operation(summary = "설문 답변 일괄 저장", description = "로그인 유저가 모든 설문에 대해 선택한 답변을 한 번에 저장합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "설문 답변 일괄 저장 요청 DTO",
            required = true,
            content = @Content(
                schema = @Schema(implementation = SurveyAnswersSubmitRequestDto.class),
                examples = @ExampleObject(
                    name = "설문 답변 15개 예시",
                    value = """
                    {
                      "answers": [
                        {"questionId": 1, "optionId": 7},
                        {"questionId": 2, "optionId": 10},
                        {"questionId": 3, "optionId": 13},
                        {"questionId": 4, "optionId": 16},
                        {"questionId": 5, "optionId": 19},
                        {"questionId": 6, "optionId": 22},
                        {"questionId": 7, "optionId": 25},
                        {"questionId": 8, "optionId": 28},
                        {"questionId": 9, "optionId": 31},
                        {"questionId": 10, "optionId": 34},
                        {"questionId": 11, "optionId": 37},
                        {"questionId": 12, "optionId": 40},
                        {"questionId": 13, "optionId": 43},
                        {"questionId": 14, "optionId": 46},
                        {"questionId": 15, "optionId": 1}
                      ]
                    }
                    """
                )
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
                        "result": "설문 답변이 저장되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 답변",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "유효하지 않은 답변",
                    summary = "필수 값 누락 등",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-400-001",
                        "message": "유효하지 않은 설문 답변입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문항/옵션 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "문항 없음",
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
            responseCode = "404",
            description = "옵션 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "옵션 없음",
                    summary = "설문 선택지를 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-404-003",
                        "message": "설문 선택지를 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "중복 답변",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "중복 답변",
                    summary = "이미 답변한 설문",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-409-001",
                        "message": "이미 답변한 설문입니다."
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
                    summary = "DB 저장 오류",
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
    @PostMapping("/answers/bulk")
    public ResponseEntity<BaseResponse<String>> saveSurveyAnswersBulk(
            @RequestBody SurveyAnswersSubmitRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        System.out.println("answers: " + dto.getAnswers());
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.saveSurveyAnswersBulk(dto, userDetails.getUserId())));
    }

    @Operation(summary = "내 설문 이력+답변 전체 조회", description = "로그인 유저의 모든 설문 이력과 각 이력의 답변(질문/옵션/키값 포함)을 반환합니다.")
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
                        "result": [ /* ... */ ]
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

    @Operation(summary = "설문 답변 목록 조회", description = "설문 이력 ID로 해당 이력의 모든 답변을 조회합니다.")
    @Parameter(name = "historyId", description = "설문 이력 ID", required = true)
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
                        "result": [ /* ... */ ]
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
                        "code": "SURVEY-404-004",
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
                        "code": "SURVEY-500-001",
                        "message": "설문 정보를 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/answers/history/{historyId}")
    public ResponseEntity<BaseResponse<List<SurveyAnswerResponseDto>>> getSurveyAnswersByHistory(@PathVariable Integer historyId) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.getSurveyAnswersByHistory(historyId)));
    }

    @Operation(summary = "FasiAPI 설문 답변 분석", description = "텍스트 답변과 신체정보를 받아 FasiAPI 서버로 전송, 분석 결과를 반환합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "FasiAPI 분석용 텍스트 답변+신체정보 요청 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = SurveyTextAnswersRequestDto.class))
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
                    summary = "FasiAPI 분석 결과 반환",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": { /* ... */ }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "502",
            description = "FasiAPI 호출 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "FasiAPI 호출 실패",
                    summary = "FasiAPI 서버와 통신 실패",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "SURVEY-502-001",
                        "message": "FASI API 호출에 실패했습니다."
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
    @PostMapping("/answers/fasi")
    public ResponseEntity<BaseResponse<SurveyResultFasiApiResponseDto>> analyzeSurveyAnswersWithFasi(
            @RequestBody SurveyTextAnswersRequestDto dto
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyService.analyzeSurveyAnswersWithFasi(dto)));
    }
} 