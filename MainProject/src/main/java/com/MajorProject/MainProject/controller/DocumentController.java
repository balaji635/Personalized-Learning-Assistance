package com.MajorProject.MainProject.controller;

import com.MajorProject.MainProject.dto.ApiResponse;
import com.MajorProject.MainProject.model.Document;
import com.MajorProject.MainProject.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // POST /api/documents/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Document>> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {

        Document document = documentService.uploadDocument(userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded and processed", document));
    }

    // GET /api/documents
    @GetMapping
    public ResponseEntity<ApiResponse<List<Document>>> getDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Document> documents = documentService.getUserDocuments(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved", documents));
    }

    // GET /api/documents/{id}/download — returns raw file bytes
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Document doc = documentService.getUserDocuments(userDetails.getUsername())
                .stream().filter(d -> d.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found"));

        byte[] data = documentService.downloadDocument(userDetails.getUsername(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                .body(data);
    }

    // DELETE /api/documents/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        documentService.deleteDocument(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}