package com.att.tdp.issueflow.dto.request;

import com.att.tdp.issueflow.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String fullName;
    @NotNull
    private Role role;
    private String password;
}
