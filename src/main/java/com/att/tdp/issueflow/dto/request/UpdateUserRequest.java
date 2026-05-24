package com.att.tdp.issueflow.dto.request;

import com.att.tdp.issueflow.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private Role role;
}
