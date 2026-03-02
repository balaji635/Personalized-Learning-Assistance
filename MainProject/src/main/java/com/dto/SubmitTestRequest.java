package com.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SubmitTestRequest {
    // Map of questionId -> selectedOptionIndex (0-3)
    private Map<Long, Integer> answers;
}