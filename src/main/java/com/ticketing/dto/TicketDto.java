package com.ticketing.dto;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private List<String> allComments;
    private String latestComment;
    private Date latestActionDate;
    private Long daysSinceLatestAction;
    private Boolean displayReopenAndCloseButton;
    private Long closedByUserId;
    private String closedByUserName;


    // Constructors
    public TicketDto() {
    }

    
    // Closed and open Tickets?
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
            String category, List<String> allComments, List<Attachment> attachments) {
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
        this.allComments = allComments;
        this.attachments = attachments;
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
    
    public TicketDto(Long ticketId, String title, String description, Long createdByUserId, String createdByUserName,
            Long assigneeUserId, String assigneeUserName, String priority, String status, Date creationDate,
            String category, Date latestActionDate, Long daysSinceLatestAction, Boolean displayReopenAndCloseButton) 
    {
		this(ticketId, title, description, createdByUserId, createdByUserName, assigneeUserId, assigneeUserName,
		    priority, status, creationDate, category);
		this.latestActionDate = latestActionDate;
		this.daysSinceLatestAction = daysSinceLatestAction;
		this.displayReopenAndCloseButton = displayReopenAndCloseButton;
	}
    

    // Closed Tickets:
    public TicketDto(Long ticketId, String title, String description, String status, Long closedByUserId, String closedByUserName) { 
    	this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.closedByUserId = closedByUserId;
        this.closedByUserName = closedByUserName;
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
    
    public List<String> getAllComments() {
        return allComments;
    }

    public void setAllComments(List<String> allComments) {
        this.allComments = allComments;
    }
    
    public Date getLatestActionDate() {
        return latestActionDate;
    }

    public void setLatestActionDate(Date latestActionDate) {
        this.latestActionDate = latestActionDate;
    }

    public Long getDaysSinceLatestAction() {
        return daysSinceLatestAction;
    }

    public void setDaysSinceLatestAction(Long daysSinceLatestAction) {
        this.daysSinceLatestAction = daysSinceLatestAction;
    }
    
    public void setDisplayReopenAndCloseButton(Boolean displayReopenAndCloseButton) {
    	this.displayReopenAndCloseButton = displayReopenAndCloseButton;
    }
    
    public Boolean getDisplayReopenAndCloseButton() {
    	return this.displayReopenAndCloseButton;
    }
    
    public Long getClosedByUserId() {
        return closedByUserId;
    }

    public void setClosedByUserId(Long closedByUserId) {
        this.closedByUserId = closedByUserId;
    }

    public String getClosedByUserName() {
        return closedByUserName;
    }

    public void setClosedByUserName(String closedByUserName) {
        this.closedByUserName = closedByUserName;
    }
}