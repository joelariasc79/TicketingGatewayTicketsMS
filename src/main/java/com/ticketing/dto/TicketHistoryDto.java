package com.ticketing.dto;

import java.util.Date;

public class TicketHistoryDto {
    private Long ticketHistoryId;
    private Long ticketId; // Just the ID for reference
    private String action;
    private Long actionByEmployeeId; // Only transfer the ID
    private String actionByEmployeeName; // Optionally transfer the name
    private Date actionDate;
    private String comments;

    // Constructors
    public TicketHistoryDto() {
    }

    public TicketHistoryDto(Long ticketHistoryId, Long ticketId, String action, Long actionByEmployeeId, String actionByEmployeeName, Date actionDate, String comments) {
        this.ticketHistoryId = ticketHistoryId;
        this.ticketId = ticketId;
        this.action = action;
        this.actionByEmployeeId = actionByEmployeeId;
        this.actionByEmployeeName = actionByEmployeeName;
        this.actionDate = actionDate;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getTicketHistoryId() {
        return ticketHistoryId;
    }

    public void setTicketHistoryId(Long ticketHistoryId) {
        this.ticketHistoryId = ticketHistoryId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getActionByEmployeeId() {
        return actionByEmployeeId;
    }

    public void setActionByEmployeeId(Long actionByEmployeeId) {
        this.actionByEmployeeId = actionByEmployeeId;
    }

    public String getActionByEmployeeName() {
        return actionByEmployeeName;
    }

    public void setActionByEmployeeName(String actionByEmployeeName) {
        this.actionByEmployeeName = actionByEmployeeName;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}