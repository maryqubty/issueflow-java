package com.att.tdp.issueflow.dto.response;

import com.att.tdp.issueflow.enums.Priority;
import com.att.tdp.issueflow.enums.TicketStatus;
import com.att.tdp.issueflow.enums.TicketType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private TicketType type;
    private Long projectId;
    private Long assigneeId;
    private LocalDateTime dueDate;
    @JsonProperty("isOverdue")
    private boolean overdue;
}
