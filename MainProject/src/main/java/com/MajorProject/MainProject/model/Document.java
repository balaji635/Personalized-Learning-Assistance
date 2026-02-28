package com.MajorProject.MainProject.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    // Path where file is stored on disk
    @Column(nullable = false)
    private String filePath;

    // PDF, DOCX, TXT
    @Column(nullable = false)
    private String fileType;

    // File size in bytes
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PROCESSING;

    // Number of chunks embedded into vector store
    @Builder.Default
    private Integer chunkCount = 0;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    // ----------------------------------------
    // Relationships
    // ----------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ----------------------------------------
    // Enum
    // ----------------------------------------
    public enum DocumentStatus {
        PROCESSING,   // file uploaded, embedding in progress
        READY,        // embedding done, ready to use in RAG
        FAILED        // embedding failed
    }
}