package com.yourmode.yourmodebackend.domain.request.dto.editor.request;

import lombok.*;

// 요청 상태 변경에 사용할 요청 DTO (필요시)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRequestStatusDto {
    private Integer requestId;   // 컨텐츠 요청 ID
    private String statusCode;   // 변경할 상태 코드
    private Long editorId;       // 변경한 에디터(사용자) ID
}
