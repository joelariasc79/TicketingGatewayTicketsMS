package com.ticketing.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "Attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonBackReference("attachments") // Add this, matching the @JsonManagedReference in Ticket
    private Ticket ticket;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(nullable = false)
    private LocalDateTime uploadDateTime;

    @Column(nullable = true) // Added FilePath field, nullable initially
    private String filePath;

    // Constructors

    public Attachment() {
        this.uploadDateTime = LocalDateTime.now();
    }

    public Attachment(Ticket ticket, String fileName, String fileType, byte[] data) {
        this.ticket = ticket;
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.uploadDateTime = LocalDateTime.now();
    }

    public Attachment(Ticket ticket, String fileName, String fileType, byte[] data, String filePath) {
        this.ticket = ticket;
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.uploadDateTime = LocalDateTime.now();
        this.filePath = filePath;
    }

    // Getters and Setters

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public LocalDateTime getUploadDateTime() {
        return uploadDateTime;
    }

    public void setUploadDateTime(LocalDateTime uploadDateTime) {
        this.uploadDateTime = uploadDateTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Optional: Override equals and hashCode if needed for specific comparisons

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return attachmentId != null ? attachmentId.equals(that.attachmentId) : that.attachmentId == null;
    }

    @Override
    public int hashCode() {
        return attachmentId != null ? attachmentId.hashCode() : 0;
    }
}