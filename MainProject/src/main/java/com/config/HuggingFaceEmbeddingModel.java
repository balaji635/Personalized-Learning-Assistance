package com.config;

import com.service.HuggingFaceEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResultMetadata;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps HuggingFaceEmbeddingService as a Spring AI EmbeddingModel bean.
 *
 * @Primary ensures PgVectorStore uses THIS instead of OpenAI's embedding model.
 * This means vectorStore.add() will use HuggingFace — no OpenAI embedding calls.
 */
@Primary
@Component
@RequiredArgsConstructor
public class HuggingFaceEmbeddingModel implements EmbeddingModel {

    private final HuggingFaceEmbeddingService huggingFaceEmbeddingService;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts = request.getInstructions();
        List<float[]> embeddings = huggingFaceEmbeddingService.embedBatch(texts);

        List<Embedding> results = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            results.add(new Embedding(embeddings.get(i), i, EmbeddingResultMetadata.EMPTY));
        }

        return new EmbeddingResponse(results);
    }

    @Override
    public float[] embed(String text) {
        return huggingFaceEmbeddingService.embed(text);
    }

    @Override
    public float[] embed(Document document) {
        return huggingFaceEmbeddingService.embed(document.getText());
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        return huggingFaceEmbeddingService.embedBatch(texts);
    }

    @Override
    public int dimensions() {
        return 384; // all-MiniLM-L6-v2 output size
    }

    private List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float f : arr) list.add(f);
        return list;
    }
}