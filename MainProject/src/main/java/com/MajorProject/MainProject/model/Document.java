package com.MajorProject.MainProject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    // ← Store raw file bytes directly in PostgreSQL (bytea column)
    // No filesystem needed — file lives entirely in the DB
    @JsonIgnore  // never send binary data in API responses
    @Lob
    @Column(name = "file_data", nullable = false)
    private byte[] fileData;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    // ----------------------------------------
    // Relationships
    // ----------------------------------------
    @JsonIgnoreProperties({"password", "conversations", "documents", "testSessions"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ----------------------------------------
    // Enum
    // ----------------------------------------
    public enum DocumentStatus {
        PROCESSING, READY, FAILED
    }
}