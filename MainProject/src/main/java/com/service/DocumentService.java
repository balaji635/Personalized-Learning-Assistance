package com.service;

import com.model.Document;
import com.model.User;
import com.repositry.DocumentRepository;
import com.repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;
    // ← No HuggingFaceEmbeddingService here — vectorStore.add() handles it via @Primary bean

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;
    private static final int MAX_FILE_SIZE_MB = 10;

    @Transactional
    public Document uploadDocument(String email, MultipartFile file) throws IOException {

        User user = getUser(email);
        validateFile(file);

        byte[] fileBytes = file.getBytes();

        Document document = Document.builder()
                .user(user)
                .originalFileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileData(fileBytes)
                .status(Document.DocumentStatus.PROCESSING)
                .build();
        document = documentRepository.save(document);

        try {
            String extractedText = extractText(fileBytes, file.getContentType(), file.getOriginalFilename());
            log.debug("Extracted {} chars from {}", extractedText.length(), file.getOriginalFilename());

            List<String> chunks = chunkText(extractedText);
            log.debug("Created {} chunks", chunks.size());

            storeChunksInVectorStore(chunks, document, user);

            document.setChunkCount(chunks.size());
            document.setStatus(Document.DocumentStatus.READY);
            document = documentRepository.save(document);

            log.info("Document processed: {} ({} chunks) for user {}",
                    file.getOriginalFilename(), chunks.size(), email);

        } catch (Exception e) {
            log.error("Failed to process document: {}", e.getMessage());
            document.setStatus(Document.DocumentStatus.FAILED);
            documentRepository.save(document);
            throw new RuntimeException("Document processing failed: " + e.getMessage());
        }

        return document;
    }

    public List<Document> getUserDocuments(String email) {
        User user = getUser(email);
        return documentRepository.findByUserIdOrderByUploadedAtDesc(user.getId());
    }

    public byte[] downloadDocument(String email, Long documentId) {
        User user = getUser(email);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied");
        return document.getFileData();
    }

    @Transactional
    public void deleteDocument(String email, Long documentId) {
        User user = getUser(email);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied");
        documentRepository.delete(document);
    }

    public String searchRelevantContext(String query, String userIdStr, int topK) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .filterExpression("userId == '" + userIdStr + "'")
                    .build();

            List<org.springframework.ai.document.Document> results =
                    vectorStore.similaritySearch(searchRequest);

            if (results.isEmpty()) return "";

            StringBuilder context = new StringBuilder();
            context.append("Relevant information from uploaded documents:\n\n");
            for (org.springframework.ai.document.Document doc : results) {
                context.append(doc.getText()).append("\n\n---\n\n");
            }
            return context.toString();

        } catch (Exception e) {
            log.warn("Vector search failed: {}", e.getMessage());
            return "";
        }
    }

    // ----------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------

    private String extractText(byte[] fileBytes, String contentType, String fileName) throws IOException {
        if (contentType == null) contentType = "";

        if (contentType.contains("pdf")) {
            return extractFromPdf(fileBytes);
        }

        // TXT, Markdown, DOCX (basic)
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    private String extractFromPdf(byte[] fileBytes) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(fileBytes)) {
            return new PDFTextStripper().getText(pdDocument);
        }
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int start = 0; start < length; start += CHUNK_SIZE - CHUNK_OVERLAP) {
            int end = Math.min(start + CHUNK_SIZE, length);
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) chunks.add(chunk);
            if (end == length) break;
        }
        return chunks;
    }

    private void storeChunksInVectorStore(List<String> chunks, Document document, User user) {
        List<org.springframework.ai.document.Document> aiDocs = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", document.getId().toString());
            metadata.put("userId", user.getId().toString());
            metadata.put("fileName", document.getOriginalFileName());
            metadata.put("chunkIndex", i);

            aiDocs.add(new org.springframework.ai.document.Document(chunks.get(i), metadata));
        }

        // vectorStore.add() calls HuggingFaceEmbeddingModel (@Primary) automatically
        vectorStore.add(aiDocs);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        long maxBytes = (long) MAX_FILE_SIZE_MB * 1024 * 1024;
        if (file.getSize() > maxBytes)
            throw new RuntimeException("File too large. Max: " + MAX_FILE_SIZE_MB + "MB");
        List<String> allowed = List.of(
                "application/pdf", "text/plain", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/markdown"
        );
        if (file.getContentType() == null || !allowed.contains(file.getContentType()))
            throw new RuntimeException("Unsupported file type. Allowed: PDF, TXT, DOC, DOCX, MD");
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}