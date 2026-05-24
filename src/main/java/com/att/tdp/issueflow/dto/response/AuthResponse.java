package com.att.tdp.issueflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
