package com.service;

import com.exception.BadRequestException;
import com.exception.NotFoundException;
import com.model.Document;
import com.model.TestSession;
import com.model.User;
import com.repositry.DocumentRepository;
import com.repositry.TestSessionRepository;
import com.repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;
    private final TestSessionRepository testSessionRepository;

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;
    private static final int MAX_FILE_SIZE_MB = 10;

    @Transactional
    public Document uploadDocument(String email, MultipartFile file) throws IOException {
        User user = getUser(email);
        validateFile(file);

        String normalizedContentType = normalizeContentType(file.getContentType());
        String originalFileName = file.getOriginalFilename();
        byte[] fileBytes = file.getBytes();

        Document document = Document.builder()
                .user(user)
                .originalFileName(originalFileName)
                .fileType(normalizedContentType.isBlank() ? MediaType.APPLICATION_OCTET_STREAM_VALUE : normalizedContentType)
                .fileSize(file.getSize())
                .fileData(fileBytes)
                .status(Document.DocumentStatus.PROCESSING)
                .build();
        document = documentRepository.save(document);

        try {
            List<ChunkSegment> chunkSegments = extractAndChunkContent(fileBytes, normalizedContentType, originalFileName);
            if (chunkSegments.isEmpty()) {
                throw new BadRequestException("No readable text chunks found in this document");
            }

            storeChunksInVectorStore(chunkSegments, document, user);

            document.setChunkCount(chunkSegments.size());
            document.setStatus(Document.DocumentStatus.READY);
            document = documentRepository.save(document);

            log.info("Document processed: {} ({} chunks) for user {}",
                    file.getOriginalFilename(), chunkSegments.size(), email);

        } catch (BadRequestException e) {
            markFailed(document);
            throw e;
        } catch (IOException e) {
            markFailed(document);
            throw new BadRequestException("Unable to read this document. Please upload a readable PDF, TXT, DOC, DOCX, or MD file.");
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", file.getOriginalFilename(), e.getMessage(), e);
            markFailed(document);
            throw new IllegalStateException("Document processing failed");
        }

        return document;
    }

    @Transactional(readOnly = true)
    public List<Document> getUserDocuments(String email) {
        User user = getUser(email);
        return documentRepository.findByUserIdOrderByUploadedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public byte[] downloadDocument(String email, Long documentId) {
        return getDocumentForUser(email, documentId).getFileData();
    }

    @Transactional(readOnly = true)
    public Document getDocumentForUser(String email, Long documentId) {
        User user = getUser(email);
        return documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    @Transactional
    public void deleteDocument(String email, Long documentId) {
        Document document = getDocumentForUser(email, documentId);
        detachDocumentFromTests(document.getId());
        deleteChunksFromVectorStore(document);
        documentRepository.delete(document);
    }

    public String searchRelevantContext(String query, String userIdStr, int topK) {
        try {
            int safeTopK = Math.max(1, topK);
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(safeTopK)
                    .filterExpression("userId == '" + userIdStr + "'")
                    .build();

            List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(searchRequest);
            if (results == null || results.isEmpty()) {
                return "";
            }

            StringBuilder context = new StringBuilder();
            context.append("Relevant information from uploaded documents:\n\n");

            int included = 0;
            for (org.springframework.ai.document.Document resultDoc : results) {
                String text = resultDoc.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }

                Map<String, Object> metadata = resultDoc.getMetadata();
                String fileName = metadataValueAsString(metadata, "fileName");
                String pageNumber = metadataValueAsString(metadata, "pageNumber");
                String chunkIndex = metadataValueAsString(metadata, "chunkIndex");

                context.append("[Source");
                if (!fileName.isBlank()) {
                    context.append(": ").append(fileName);
                }
                if (!pageNumber.isBlank()) {
                    context.append(" | page ").append(pageNumber);
                }
                if (!chunkIndex.isBlank()) {
                    context.append(" | chunk ").append(chunkIndex);
                }
                context.append("]\n");
                context.append(text.trim()).append("\n\n---\n\n");

                included++;
                if (included >= safeTopK) {
                    break;
                }
            }

            return included == 0 ? "" : context.toString();

        } catch (Exception e) {
            log.warn("Vector search failed: {}", e.getMessage());
            return "";
        }
    }

    private List<ChunkSegment> extractAndChunkContent(byte[] fileBytes, String contentType, String fileName) throws IOException {
        String normalizedContentType = normalizeContentType(contentType);
        String extension = extractExtension(fileName);

        if (normalizedContentType.contains("pdf") || ".pdf".equals(extension)) {
            return extractAndChunkPdf(fileBytes);
        }

        String extractedText;
        if (normalizedContentType.contains("wordprocessingml.document") || ".docx".equals(extension)) {
            extractedText = extractFromDocx(fileBytes);
        } else if ("application/msword".equals(normalizedContentType) || ".doc".equals(extension)) {
            extractedText = extractFromDoc(fileBytes);
        } else {
            extractedText = new String(fileBytes, StandardCharsets.UTF_8);
        }

        if (extractedText == null || extractedText.isBlank()) {
            return List.of();
        }

        List<ChunkSegment> chunkSegments = new ArrayList<>();
        for (String chunk : chunkText(extractedText)) {
            chunkSegments.add(new ChunkSegment(chunk, null));
        }
        return chunkSegments;
    }

    private List<ChunkSegment> extractAndChunkPdf(byte[] fileBytes) throws IOException {
        List<ChunkSegment> chunkSegments = new ArrayList<>();

        try (PDDocument pdDocument = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdDocument.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(pdDocument);
                if (pageText == null || pageText.isBlank()) {
                    continue;
                }

                for (String chunk : chunkText(pageText)) {
                    chunkSegments.add(new ChunkSegment(chunk, page));
                }
            }
        }

        return chunkSegments;
    }

    private String extractFromDocx(byte[] fileBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractFromDoc(byte[] fileBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
             HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        int step = CHUNK_SIZE - CHUNK_OVERLAP;
        if (step <= 0) {
            step = CHUNK_SIZE;
        }

        int length = text.length();
        for (int start = 0; start < length; start += step) {
            int end = Math.min(start + CHUNK_SIZE, length);
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end == length) {
                break;
            }
        }
        return chunks;
    }

    private void storeChunksInVectorStore(List<ChunkSegment> chunks, Document document, User user) {
        if (chunks.isEmpty()) {
            return;
        }

        List<org.springframework.ai.document.Document> aiDocuments = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            ChunkSegment segment = chunks.get(i);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", document.getId().toString());
            metadata.put("userId", user.getId().toString());
            metadata.put("fileName", document.getOriginalFileName());
            metadata.put("chunkIndex", i);
            if (segment.pageNumber() != null) {
                metadata.put("pageNumber", segment.pageNumber());
            }

            aiDocuments.add(new org.springframework.ai.document.Document(segment.text(), metadata));
        }

        vectorStore.add(aiDocuments);
    }

    private void detachDocumentFromTests(Long documentId) {
        List<TestSession> sessions = testSessionRepository.findBySourceDocumentId(documentId);
        if (sessions.isEmpty()) {
            return;
        }

        for (TestSession session : sessions) {
            session.setSourceDocument(null);
        }

        testSessionRepository.saveAll(sessions);
    }

    private void deleteChunksFromVectorStore(Document document) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        var expression = builder.and(
                builder.eq("documentId", document.getId().toString()),
                builder.eq("userId", document.getUser().getId().toString())
        ).build();

        try {
            vectorStore.delete(expression);
        } catch (Exception e) {
            log.error("Failed to delete vector embeddings for document {}", document.getId(), e);
            throw new IllegalStateException("Failed to delete document embeddings");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        long maxBytes = (long) MAX_FILE_SIZE_MB * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("File too large. Max: " + MAX_FILE_SIZE_MB + "MB");
        }

        String contentType = normalizeContentType(file.getContentType());
        String originalName = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);

        Set<String> allowedContentTypes = Set.of(
                "application/pdf",
                "application/x-pdf",
                "text/plain",
                "text/markdown",
                "text/x-markdown",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        Set<String> allowedExtensions = Set.of(".pdf", ".txt", ".md", ".doc", ".docx");

        boolean contentTypeAllowed = !contentType.isBlank() && allowedContentTypes.contains(contentType);
        boolean extensionAllowed = allowedExtensions.stream().anyMatch(originalName::endsWith);

        if (!contentTypeAllowed && !extensionAllowed) {
            throw new BadRequestException("Unsupported file type. Allowed: PDF, TXT, DOC, DOCX, MD");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeContentType(String rawContentType) {
        if (rawContentType == null) {
            return "";
        }

        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        int separatorIndex = normalized.indexOf(';');
        if (separatorIndex >= 0) {
            normalized = normalized.substring(0, separatorIndex).trim();
        }
        return normalized;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String metadataValueAsString(Map<String, Object> metadata, String key) {
        if (metadata == null || !metadata.containsKey(key) || metadata.get(key) == null) {
            return "";
        }
        return String.valueOf(metadata.get(key)).trim();
    }

    private void markFailed(Document document) {
        document.setStatus(Document.DocumentStatus.FAILED);
        documentRepository.save(document);
    }

    private record ChunkSegment(String text, Integer pageNumber) {
    }
}