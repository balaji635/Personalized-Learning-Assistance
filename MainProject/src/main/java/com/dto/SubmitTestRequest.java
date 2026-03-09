package com.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitTestRequest {

    @NotNull(message = "Answers are required")
    private Map<Long, Integer> answers;
}
