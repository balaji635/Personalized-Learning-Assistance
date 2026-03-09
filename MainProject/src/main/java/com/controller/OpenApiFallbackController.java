package com.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@ConditionalOnMissingClass("org.springdoc.core.properties.SpringDocConfigProperties")
public class OpenApiFallbackController {

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> apiDocs(HttpServletRequest request) {
        Map<String, Object> openApi = new LinkedHashMap<>();
        openApi.put("openapi", "3.0.1");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Learning Assistant API");
        info.put("version", "1.0.0");
        info.put("description", "Fallback API docs endpoint when Springdoc is not present.");
        openApi.put("info", info);

        String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        openApi.put("servers", new Object[]{Map.of("url", serverUrl)});

        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/api/auth/register", operation("post", "Register a user"));
        paths.put("/api/auth/login", operation("post", "Login"));
        paths.put("/api/auth/refresh", operation("post", "Refresh token"));
        paths.put("/api/auth/me", operation("get", "Get current user"));
        paths.put("/api/auth/logout", operation("post", "Logout"));
        paths.put("/api/conversations", mergeOperations(operation("post", "Create conversation"), operation("get", "List conversations")));
        paths.put("/api/conversations/{id}/messages", operation("get", "Get conversation messages"));
        paths.put("/api/conversations/{id}", operation("delete", "Delete conversation"));
        paths.put("/api/chat/{conversationId}", operation("post", "Send chat message"));
        paths.put("/api/documents", operation("get", "List documents"));
        paths.put("/api/documents/upload", operation("post", "Upload document"));
        paths.put("/api/documents/{id}/download", operation("get", "Download document"));
        paths.put("/api/documents/{id}", operation("delete", "Delete document"));
        paths.put("/api/tests/generate", operation("post", "Generate test"));
        paths.put("/api/tests", operation("get", "List tests"));
        paths.put("/api/tests/{id}/submit", operation("post", "Submit test"));
        paths.put("/api/tests/{id}/results", operation("get", "Get test results"));

        openApi.put("paths", paths);
        openApi.put("components", new LinkedHashMap<>());
        return openApi;
    }

    private Map<String, Object> operation(String method, String summary) {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("summary", summary);
        op.put("responses", Map.of("200", Map.of("description", "OK")));
        return Map.of(method, op);
    }

    private Map<String, Object> mergeOperations(Map<String, Object> first, Map<String, Object> second) {
        Map<String, Object> merged = new LinkedHashMap<>();
        merged.putAll(first);
        merged.putAll(second);
        return merged;
    }
}
