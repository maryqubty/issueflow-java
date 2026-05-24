package com.att.tdp.issueflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private String filename;
    private String contentType;
}
