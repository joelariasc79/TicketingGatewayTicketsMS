package com.ticketing.dto;

public class NewTicketDto {
    private String title;
    private String description;
    private Long createdByEmployeeId;
    private String priority;
    private String status;
    private String category;

    // Constructors
    public NewTicketDto() {
    }

    public NewTicketDto(String title, String description, Long createdByEmployeeId, String priority, String status, String category) {
        this.title = title;
        this.description = description;
        this.createdByEmployeeId = createdByEmployeeId;
        this.priority = priority;
        this.status = status;
        this.category = category;
    }

    // Getters and Setters
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

    public Long getCreatedByEmployeeId() {
        return createdByEmployeeId;
    }

    public void setCreatedByEmployeeId(Long createdByEmployeeId) {
        this.createdByEmployeeId = createdByEmployeeId;
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
}