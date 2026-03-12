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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;
    private final TestSessionRepository testSessionRepository;

    // ── Chunking config ───────────────────────────────────────────
    private static final int MAX_CHUNK_SIZE  = 300;  // max chars per chunk
    private static final int MIN_CHUNK_SIZE  = 80;  // ignore chunks smaller than this
    private static final int CHUNK_OVERLAP   = 60;   // overlap between chunks

    private static final int MAX_FILE_SIZE_MB = 10;

    // ── Stored for ChatService hallucination metrics ───────────────
    private double lastAvgRetrievalScore = 0.0;

    public double getLastAvgRetrievalScore() {
        return lastAvgRetrievalScore;
    }

    // ═════════════════════════════════════════════════════════════
    // UPLOAD
    // ═════════════════════════════════════════════════════════════
    @Transactional
    public Document uploadDocument(String email, MultipartFile file) throws IOException {
        User user = getUser(email);
        validateFile(file);

        String normalizedContentType = normalizeContentType(file.getContentType());
        String originalFileName      = file.getOriginalFilename();
        byte[] fileBytes             = file.getBytes();

        Document document = Document.builder()
                .user(user)
                .originalFileName(originalFileName)
                .fileType(normalizedContentType.isBlank()
                        ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                        : normalizedContentType)
                .fileSize(file.getSize())
                .fileData(fileBytes)
                .status(Document.DocumentStatus.PROCESSING)
                .build();
        document = documentRepository.save(document);

        try {
            String extractedText = extractText(fileBytes, normalizedContentType, originalFileName);
            if (extractedText.isBlank()) {
                throw new BadRequestException("No readable text found in this document");
            }

            // ── RECURSIVE CHUNKING ────────────────────────────────
            List<String> chunks = recursiveChunk(extractedText);
            if (chunks.isEmpty()) {
                throw new BadRequestException("No readable text chunks found in this document");
            }

            log.info("╔══ CHUNKING RESULT ════════════════════════╗");
            log.info("║ File         : {}", originalFileName);
            log.info("║ Total chars  : {}", extractedText.length());
            log.info("║ Chunks made  : {}", chunks.size());
            log.info("║ Avg chunk sz : {} chars",
                    chunks.stream().mapToInt(String::length).average().orElse(0));
            log.info("╚═══════════════════════════════════════════╝");

            storeChunksInVectorStore(chunks, document, user);

            document.setChunkCount(chunks.size());
            document.setStatus(Document.DocumentStatus.READY);
            document = documentRepository.save(document);

            log.info("Document processed: {} ({} chunks) for user {}",
                    originalFileName, chunks.size(), email);

        } catch (BadRequestException e) {
            markFailed(document);
            throw e;
        } catch (IOException e) {
            markFailed(document);
            throw new BadRequestException(
                    "Unable to read this document. Please upload a readable PDF, TXT, DOC, DOCX, or MD file.");
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", originalFileName, e.getMessage(), e);
            markFailed(document);
            throw new IllegalStateException("Document processing failed");
        }

        return document;
    }

    // ═════════════════════════════════════════════════════════════
    // RECURSIVE CHARACTER CHUNKING
    // Strategy: try to split at natural boundaries first
    //   1. Double newline  (paragraph break)
    //   2. Single newline  (line break)
    //   3. Period/sentence (. ! ?)
    //   4. Comma/clause    (, ; :)
    //   5. Space           (word boundary)
    //   6. Hard cut        (absolute last resort)
    // ═════════════════════════════════════════════════════════════
    private List<String> recursiveChunk(String text) {

        // Separators in priority order — most meaningful first
        List<String> separators = List.of(
                "\n\n",   // paragraph
                "\n",     // new line
                ". ",     // sentence end
                "! ",     // exclamation
                "? ",     // question
                "; ",     // semicolon
                ", ",     // comma
                " ",      // word boundary
                ""        // hard cut (last resort)
        );

        List<String> finalChunks = new ArrayList<>();
        splitRecursively(text.trim(), separators, 0, finalChunks);

        // Add overlap between consecutive chunks
        return addOverlap(finalChunks);
    }

    /**
     * Recursively splits text using the separator at index separatorIdx.
     * If a piece is still too large, tries the next separator.
     * If small enough, adds to finalChunks directly.
     */
    private void splitRecursively(String text,
                                  List<String> separators,
                                  int separatorIdx,
                                  List<String> finalChunks) {

        // Base case — fits in one chunk
        if (text.length() <= MAX_CHUNK_SIZE) {
            if (text.length() >= MIN_CHUNK_SIZE) {
                finalChunks.add(text.trim());
            }
            return;
        }

        // No more separators to try → hard cut
        if (separatorIdx >= separators.size()) {
            hardCut(text, finalChunks);
            return;
        }

        String separator = separators.get(separatorIdx);

        // Split by current separator
        List<String> parts;
        if (separator.isEmpty()) {
            // Hard character cut
            hardCut(text, finalChunks);
            return;
        } else {
            parts = Arrays.asList(text.split(java.util.regex.Pattern.quote(separator), -1));
        }

        // If separator produced only one part (not found), try next separator
        if (parts.size() <= 1) {
            splitRecursively(text, separators, separatorIdx + 1, finalChunks);
            return;
        }

        // Merge small parts together, recurse on large ones
        StringBuilder currentBuffer = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) continue;

            String candidate = currentBuffer.length() == 0
                    ? part
                    : currentBuffer + separator + part;

            if (candidate.length() <= MAX_CHUNK_SIZE) {
                // Still fits — keep merging
                currentBuffer = new StringBuilder(candidate);
            } else {
                // Buffer is full → flush it
                if (currentBuffer.length() >= MIN_CHUNK_SIZE) {
                    // Buffer is already a good chunk
                    finalChunks.add(currentBuffer.toString().trim());
                } else if (currentBuffer.length() > 0) {
                    // Buffer too small → recurse to merge with next separator
                    splitRecursively(currentBuffer.toString(), separators,
                            separatorIdx + 1, finalChunks);
                }

                // Start new buffer with current part
                if (part.length() > MAX_CHUNK_SIZE) {
                    // This part alone is too large → recurse on it
                    splitRecursively(part, separators, separatorIdx + 1, finalChunks);
                    currentBuffer = new StringBuilder();
                } else {
                    currentBuffer = new StringBuilder(part);
                }
            }
        }

        // Flush remaining buffer
        if (currentBuffer.length() >= MIN_CHUNK_SIZE) {
            finalChunks.add(currentBuffer.toString().trim());
        } else if (currentBuffer.length() > 0) {
            splitRecursively(currentBuffer.toString(), separators,
                    separatorIdx + 1, finalChunks);
        }
    }

    /**
     * Last resort: cuts text into fixed-size pieces with no intelligence.
     */
    private void hardCut(String text, List<String> finalChunks) {
        for (int start = 0; start < text.length(); start += MAX_CHUNK_SIZE) {
            int end   = Math.min(start + MAX_CHUNK_SIZE, text.length());
            String chunk = text.substring(start, end).trim();
            if (chunk.length() >= MIN_CHUNK_SIZE) {
                finalChunks.add(chunk);
            }
        }
    }

    /**
     * Adds overlap: takes last CHUNK_OVERLAP chars of previous chunk
     * and prepends to the next chunk so context is not lost at boundaries.
     */
    private List<String> addOverlap(List<String> chunks) {
        if (chunks.size() <= 1) return chunks;

        List<String> overlapped = new ArrayList<>();
        overlapped.add(chunks.get(0));

        for (int i = 1; i < chunks.size(); i++) {
            String prev    = chunks.get(i - 1);
            String current = chunks.get(i);

            // Take last CHUNK_OVERLAP chars from previous chunk
            String overlapText = prev.length() > CHUNK_OVERLAP
                    ? prev.substring(prev.length() - CHUNK_OVERLAP)
                    : prev;

            String withOverlap = overlapText.trim() + " " + current.trim();

            // Only add overlap if it doesn't exceed max size
            if (withOverlap.length() <= MAX_CHUNK_SIZE + CHUNK_OVERLAP) {
                overlapped.add(withOverlap.trim());
            } else {
                overlapped.add(current.trim());
            }
        }

        return overlapped;
    }

    // ═════════════════════════════════════════════════════════════
    // RAG SEARCH
    // ═════════════════════════════════════════════════════════════
    public String searchRelevantContext(String query, String userIdStr, int topK) {
        try {
            int safeTopK = Math.max(1, topK);
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(safeTopK)
                    .filterExpression("userId == '" + userIdStr + "'")
                    .build();

            List<org.springframework.ai.document.Document> results =
                    vectorStore.similaritySearch(searchRequest);

            if (results == null || results.isEmpty()) {
                log.info("╔══ RAG RETRIEVAL METRICS ══════════════════╗");
                log.info("║ Query        : {}", query);
                log.info("║ Result       : No matching chunks found ❌");
                log.info("╚═══════════════════════════════════════════╝");
                this.lastAvgRetrievalScore = 0.0;
                return "";
            }

            // FIX: use Number to handle both Float and Double from PgVector
            double avgScore = results.stream()
                    .mapToDouble(doc -> {
                        Object score = doc.getMetadata().get("distance");
                        return  score instanceof Number
                                ? 1.0-((Number) score).doubleValue() : 0.0;
                    })
                    .average().orElse(0.0);

            this.lastAvgRetrievalScore = avgScore;

            log.info("╔══ RAG RETRIEVAL METRICS ══════════════════╗");
            log.info("║ Query        : {}", query);
            log.info("║ Chunks Found : {}", results.size());
            results.forEach(doc -> {
                Object raw   = doc.getMetadata().get("distance");
                double score = raw instanceof Number ? 1.0-((Number) raw).doubleValue() : 0.0;
                String file  = (String) doc.getMetadata().get("fileName");
                Object chunk = doc.getMetadata().get("chunkIndex");
                log.info("║  └─ {} [chunk {}] → similarity: {}",
                        file, chunk, String.format("%.4f", score));
            });
            log.info("║ Avg Score    : {}", String.format("%.4f", avgScore));
            log.info("║ Quality      : {}", avgScore >= 0.75 ? "HIGH ✅" :
                    avgScore >= 0.3 ? "MEDIUM ⚠️" : "LOW ❌");
            log.info("╚═══════════════════════════════════════════╝");

            StringBuilder context = new StringBuilder();
            context.append("Relevant information from uploaded documents:\n\n");
            for (org.springframework.ai.document.Document resultDoc : results) {
                if (resultDoc.getText() == null || resultDoc.getText().isBlank()) continue;
                context.append(resultDoc.getText()).append("\n\n---\n\n");
            }

            return context.toString();

        } catch (Exception e) {
            log.warn("Vector search failed: {}", e.getMessage());
            return "";
        }
    }

    // ═════════════════════════════════════════════════════════════
    // CRUD
    // ═════════════════════════════════════════════════════════════
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

    // ═════════════════════════════════════════════════════════════
    // TEXT EXTRACTION
    // ═════════════════════════════════════════════════════════════
    private String extractText(byte[] fileBytes, String contentType, String fileName)
            throws IOException {
        String normalizedContentType = normalizeContentType(contentType);
        String extension = extractExtension(fileName);

        if (normalizedContentType.contains("pdf") || ".pdf".equals(extension)) {
            return extractFromPdf(fileBytes);
        }
        if (normalizedContentType.contains("wordprocessingml.document") || ".docx".equals(extension)) {
            return extractFromDocx(fileBytes);
        }
        if ("application/msword".equals(normalizedContentType) || ".doc".equals(extension)) {
            return extractFromDoc(fileBytes);
        }
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    private String extractFromPdf(byte[] fileBytes) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(fileBytes)) {
            return new PDFTextStripper().getText(pdDocument);
        }
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

    // ═════════════════════════════════════════════════════════════
    // VECTOR STORE HELPERS
    // ═════════════════════════════════════════════════════════════
    private void storeChunksInVectorStore(List<String> chunks, Document document, User user) {
        if (chunks.isEmpty()) return;

        List<org.springframework.ai.document.Document> aiDocuments = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", document.getId().toString());
            metadata.put("userId",     user.getId().toString());
            metadata.put("fileName",   document.getOriginalFileName());
            metadata.put("chunkIndex", i);

            aiDocuments.add(new org.springframework.ai.document.Document(chunks.get(i), metadata));
        }
        vectorStore.add(aiDocuments);
    }

    private void detachDocumentFromTests(Long documentId) {
        List<TestSession> sessions = testSessionRepository.findBySourceDocumentId(documentId);
        if (sessions.isEmpty()) return;
        for (TestSession session : sessions) session.setSourceDocument(null);
        testSessionRepository.saveAll(sessions);
    }

    private void deleteChunksFromVectorStore(Document document) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        var expression = builder.and(
                builder.eq("documentId", document.getId().toString()),
                builder.eq("userId",     document.getUser().getId().toString())
        ).build();

        try {
            vectorStore.delete(expression);
        } catch (Exception e) {
            log.error("Failed to delete vector embeddings for document {}", document.getId(), e);
            throw new IllegalStateException("Failed to delete document embeddings");
        }
    }

    // ═════════════════════════════════════════════════════════════
    // VALIDATION + UTILS
    // ═════════════════════════════════════════════════════════════
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("File is empty");

        long maxBytes = (long) MAX_FILE_SIZE_MB * 1024 * 1024;
        if (file.getSize() > maxBytes)
            throw new BadRequestException("File too large. Max: " + MAX_FILE_SIZE_MB + "MB");

        String contentType   = normalizeContentType(file.getContentType());
        String originalName  = file.getOriginalFilename() == null
                ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);

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
        boolean extensionAllowed   = allowedExtensions.stream().anyMatch(originalName::endsWith);

        if (!contentTypeAllowed && !extensionAllowed)
            throw new BadRequestException("Unsupported file type. Allowed: PDF, TXT, DOC, DOCX, MD");
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeContentType(String rawContentType) {
        if (rawContentType == null) return "";
        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        int idx = normalized.indexOf(';');
        if (idx >= 0) normalized = normalized.substring(0, idx).trim();
        return normalized;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) return "";
        return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private void markFailed(Document document) {
        document.setStatus(Document.DocumentStatus.FAILED);
        documentRepository.save(document);
    }
}