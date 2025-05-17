package com.ticketing.dto;

public class TicketHistoryCreationDto {
    private Long ticketId;
    private String action;
    private Long actionByEmployeeId;
    private String comments;

    // Constructors
    public TicketHistoryCreationDto() {
    }

    public TicketHistoryCreationDto(Long ticketId, String action, Long actionByEmployeeId, String comments) {
        this.ticketId = ticketId;
        this.action = action;
        this.actionByEmployeeId = actionByEmployeeId;
        this.comments = comments;
    }

    // Getters and Setters
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}