package com.att.tdp.issueflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull
    private Long authorId;
    @NotBlank
    private String content;
}
