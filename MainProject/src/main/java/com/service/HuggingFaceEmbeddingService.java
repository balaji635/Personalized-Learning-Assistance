package com.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HuggingFaceEmbeddingService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private static final String ROUTER_URL =
            "https://router.huggingface.co/hf-inference/models/sentence-transformers/all-MiniLM-L6-v2/pipeline/feature-extraction";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return results.isEmpty() ? new float[384] : results.get(0);
    }

    public List<float[]> embedBatch(List<String> texts) {
        try {
            // Build JSON body manually
            String requestBody = objectMapper.writeValueAsString(Map.of("inputs", texts));

            // Use raw HttpURLConnection — full control over headers, no Spring interference
            URL url = new URL(ROUTER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            // Write body
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            // Read response
            int statusCode = conn.getResponseCode();
            InputStream is = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            if (statusCode != 200) {
                log.error("HuggingFace API error {}: {}", statusCode, responseBody);
                throw new RuntimeException("HuggingFace API returned " + statusCode + ": " + responseBody);
            }

            // Parse response: List<List<Double>>
            List<List<Double>> rawEmbeddings = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)
                    )
            );

            return rawEmbeddings.stream()
                    .map(doubles -> {
                        float[] floats = new float[doubles.size()];
                        for (int i = 0; i < doubles.size(); i++) {
                            floats[i] = doubles.get(i).floatValue();
                        }
                        return floats;
                    })
                    .toList();

        } catch (Exception e) {
            log.error("HuggingFace embedding error: {}", e.getMessage());
            throw new RuntimeException("Failed to generate embeddings: " + e.getMessage());
        }
    }
}