package com.controller;

import com.dto.ApiResponse;
import com.dto.GenerateTestRequest;
import com.dto.SubmitTestRequest;
import com.model.TestSession;
import com.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TestSession>> generate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid GenerateTestRequest request) {

        TestSession session = testService.generateTest(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Test generated successfully", session));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<TestSession>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid SubmitTestRequest request) {

        TestSession session = testService.submitTest(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Test submitted successfully", session));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TestSession>>> getTests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TestSession> tests = testService.getUserTests(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Tests retrieved", tests));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ApiResponse<TestSession>> getResults(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        TestSession session = testService.getTestResults(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Results retrieved", session));
    }
}
