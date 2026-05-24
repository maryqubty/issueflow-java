package com.att.tdp.issueflow.dto.request;

import com.att.tdp.issueflow.enums.Priority;
import com.att.tdp.issueflow.enums.TicketStatus;
import com.att.tdp.issueflow.enums.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTicketRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private TicketStatus status;
    @NotNull
    private Priority priority;
    @NotNull
    private TicketType type;
    @NotNull
    private Long projectId;
    private Long assigneeId;
    private LocalDateTime dueDate;
}
