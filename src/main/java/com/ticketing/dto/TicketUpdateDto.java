package com.ticketing.dto;

public class TicketUpdateDto {
    private Long ticketId;
    private String title;
    private String description;
    private Long assigneeEmployeeId;
    private String priority;
    private String status;
    private String category;
    private String fileAttachmentPath;

    // Constructors
    public TicketUpdateDto() {
    }

    public TicketUpdateDto(Long ticketId, String title, String description, Long assigneeEmployeeId, String priority, String status, String category, String fileAttachmentPath) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.assigneeEmployeeId = assigneeEmployeeId;
        this.priority = priority;
        this.status = status;
        this.category = category;
        this.fileAttachmentPath = fileAttachmentPath;
    }

    // Getters and Setters
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssigneeEmployeeId() {
        return assigneeEmployeeId;
    }

    public void setAssigneeEmployeeId(Long assigneeEmployeeId) {
        this.assigneeEmployeeId = assigneeEmployeeId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFileAttachmentPath() {
        return fileAttachmentPath;
    }

    public void setFileAttachmentPath(String fileAttachmentPath) {
        this.fileAttachmentPath = fileAttachmentPath;
    }
}