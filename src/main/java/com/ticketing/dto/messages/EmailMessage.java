package com.ticketing.dto.messages;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList; // Import ArrayList for initializing the list

// This DTO will be sent as a JMS message payload
public class EmailMessage implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private String to;
    private String subject;
    private String body;
    private String sender; // Optional, if you want to explicitly set sender per email
    private List<Attachment> attachments; // NEW: Field to hold attachments

    public EmailMessage() {
        // Initialize attachments list even for no-arg constructor
        this.attachments = new ArrayList<>();
    }

    // Original constructor (for emails without attachments)
    public EmailMessage(String to, String subject, String body, String sender) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.sender = sender;
        this.attachments = new ArrayList<>(); // Initialize the list here too
    }

    // NEW Constructor: for emails with attachments
    public EmailMessage(String to, String subject, String body, String sender, List<Attachment> attachments) {
        this(to, subject, body, sender); // Call the base constructor
        if (attachments != null) {
            this.attachments.addAll(attachments); // Add all provided attachments
        }
    }

    // Getters and Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) { // Corrected method name: setBody
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    // NEW Getter for attachments
    public List<Attachment> getAttachments() {
        return attachments;
    }

    // NEW Setter for attachments (use with caution, better to add individually or in constructor)
    public void setAttachments(List<Attachment> attachments) {
        if (attachments == null) {
            this.attachments = new ArrayList<>(); // Ensure it's never null
        } else {
            this.attachments = new ArrayList<>(attachments); // Create a new list to avoid external modification
        }
    }

    // NEW Method: convenience to add a single attachment
    public void addAttachment(Attachment attachment) {
        if (this.attachments == null) { // Defensive check, though constructors initialize it
            this.attachments = new ArrayList<>();
        }
        if (attachment != null) {
            this.attachments.add(attachment);
        }
    }

    @Override
    public String toString() {
        return "EmailMessage{" +
               "to='" + to + '\'' +
               ", subject='" + subject + '\'' +
               ", body='" + body + '\'' +
               ", sender='" + sender + '\'' +
               ", attachments=" + (attachments != null ? attachments.size() : 0) + " attachment(s)" +
               '}';
    }

    // NEW Nested Class: Defines what an attachment consists of
    public static class Attachment implements Serializable {
        private static final long serialVersionUID = 1L;

        private String fileName;
        private String contentType; // e.g., "application/pdf", "image/jpeg", "text/plain"
        private byte[] fileContent; // The binary content of the file

        public Attachment() {
            // Default constructor for serialization frameworks if needed
        }

        public Attachment(String fileName, String contentType, byte[] fileContent) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileContent = fileContent;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) { // Setter for fileName
            this.fileName = fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) { // Setter for contentType
            this.contentType = contentType;
        }

        public byte[] getFileContent() {
            return fileContent;
        }

        public void setFileContent(byte[] fileContent) { // Setter for fileContent
            this.fileContent = fileContent;
        }

        @Override
        public String toString() {
            return "Attachment{" +
                   "fileName='" + fileName + '\'' +
                   ", contentType='" + contentType + '\'' +
                   ", fileSize=" + (fileContent != null ? fileContent.length : 0) + " bytes" +
                   '}';
        }
    }
}