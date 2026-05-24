package com.att.tdp.issueflow.dto.response;

import com.att.tdp.issueflow.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DependencyResponse {
    private Long id;
    private String title;
    private TicketStatus status;
}
