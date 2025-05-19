package com.ticketing.dto;

import java.util.Date;
import java.util.List;

import com.ticketing.domain.Attachment;
import com.ticketing.domain.TicketHistory;

public class TicketDto {
    private Long ticketId;
    private String title;
    private String description;
    private Long createdByUserId; // Only transfer the ID
    private String createdByUserName; // Optionally transfer the name
    private Long assigneeUserId;     // Only transfer the ID
    private String assigneeUserName; // Optionally transfer the name
    private String priority;
    private String status;
    private Date creationDate;
    private String category;
    private List<Attachment> attachments;
    private String latestComment;

    //  private List<TicketHistoryDto> history; // DTO for history
    //  private List<TicketHistory> history; // DTO for history

    // Constructors
    public TicketDto() {
    }


    public TicketDto(Long ticketId, String title, String description, Long createdByUserId, String createdByUserName,
            Long assigneeUserId, String assigneeUserName, String priority, String status, Date creationDate,
            String category) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.createdByUserId = createdByUserId;
        this.createdByUserName = createdByUserName;
        this.assigneeUserId = assigneeUserId;
        this.assigneeUserName = assigneeUserName;
        this.priority = priority;
        this.status = status;
        this.creationDate = creationDate;
        this.category = category;
    }

    public TicketDto(Long ticketId, String title, String description, Long createdByUserId, String createdByUserName,
            Long assigneeUserId, String assigneeUserName, String priority, String status, Date creationDate,
            String category, String latestComment, List<Attachment> attachments) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.createdByUserId = createdByUserId;
        this.createdByUserName = createdByUserName;
        this.assigneeUserId = assigneeUserId;
        this.assigneeUserName = assigneeUserName;
        this.priority = priority;
        this.status = status;
        this.creationDate = creationDate;
        this.category = category;
        this.latestComment = latestComment;
        this.attachments = attachments;
    }



    //  public TicketDto(Long ticketId, String title, String description, Long createdByUserId, String createdByUserName,
    //          Long assigneeUserId, String assigneeUserName, String priority, String status, Date creationDate,
    //          String category, List<Attachment> attachments, List<TicketHistoryDto> history) {
    //      this.ticketId = ticketId;
    //      this.title = title;
    //      this.description = description;
    //      this.createdByUserId = createdByUserId;
    //      this.createdByUserName = createdByUserName;
    //      this.assigneeUserId = assigneeUserId;
    //      this.assigneeUserName = assigneeUserName;
    //      this.priority = priority;
    //      this.status = status;
    //      this.creationDate = creationDate;
    //      this.category = category;
    //      this.attachments = attachments;
    //      this.history = history;
    //  }

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

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getAssigneeUserName() {
        return assigneeUserName;
    }

    public void setAssigneeUserName(String assigneeUserName) {
        this.assigneeUserName = assigneeUserName;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getLatestComment() {
        return latestComment;
    }

    public void setLatestComment(String latestComment) {
        this.latestComment = latestComment;
    }


    //  public List<TicketHistoryDto> getHistory() {
    //      return history;
    //  }
    //
    //  public void setHistory(List<TicketHistoryDto> history) {
    //      this.history = history;
    //  }
    //
    //  public List<TicketHistory> getHistory() {
    //      return history;
    //  }
    //
    //  public void setHistory(List<TicketHistory> history) {
    //      this.history = history;
    //  }
}