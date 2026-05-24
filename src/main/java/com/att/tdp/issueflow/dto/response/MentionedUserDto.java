package com.att.tdp.issueflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MentionedUserDto {
    private Long id;
    private String username;
    private String fullName;
}
