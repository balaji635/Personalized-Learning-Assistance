package com.controller;

import com.dto.ApiResponse;
import com.model.Document;
import com.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Document>> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {

        Document document = documentService.uploadDocument(userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded and processed", document));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Document>>> getDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Document> documents = documentService.getUserDocuments(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved", documents));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Document document = documentService.getDocumentForUser(userDetails.getUsername(), id);
        byte[] data = documentService.downloadDocument(userDetails.getUsername(), id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (document.getFileType() != null && !document.getFileType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(document.getFileType());
            } catch (InvalidMediaTypeException ex) {
                log.warn("Invalid stored media type '{}' for document {}", document.getFileType(), document.getId());
            }
        }

        String safeFileName = sanitizeFileName(document.getOriginalFileName());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(data.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(safeFileName, StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        documentService.deleteDocument(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "document.bin";
        }

        String sanitized = originalFileName
                .replace("\r", "")
                .replace("\n", "")
                .replace("\"", "");

        return sanitized.isBlank() ? "document.bin" : sanitized;
    }
}
