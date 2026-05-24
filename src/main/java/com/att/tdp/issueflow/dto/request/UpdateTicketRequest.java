package com.att.tdp.issueflow.dto.request;

import com.att.tdp.issueflow.enums.Priority;
import com.att.tdp.issueflow.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTicketRequest {
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private Long assigneeId;
    private LocalDateTime dueDate;
}
