package com.att.tdp.issueflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResultResponse {
    private int created;
    private int failed;
    private List<String> errors;
}
