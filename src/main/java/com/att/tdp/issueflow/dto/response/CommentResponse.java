package com.att.tdp.issueflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String content;
    private List<MentionedUserDto> mentionedUsers;
}
