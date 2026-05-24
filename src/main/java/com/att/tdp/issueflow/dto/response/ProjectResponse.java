package com.att.tdp.issueflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
}
