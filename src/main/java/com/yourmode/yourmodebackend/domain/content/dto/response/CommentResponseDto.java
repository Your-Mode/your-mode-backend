package com.yourmode.yourmodebackend.domain.content.dto.response;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponseDto {
    private Integer id;
    private String commentText;
    private LocalDateTime createdAt;
    private Integer userId;
    private String userName;
    private Integer contentId;
}
