package com.ticketing.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketHistoryId;


    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @JsonBackReference("history") // Add this, matching the @JsonManagedReference in Ticket
    private Ticket ticket;

    private String action; // CREATED, APPROVED, REJECTED, ASSIGNED, RESOLVED, CLOSED, REOPENED

    @ManyToOne
    @JoinColumn(name = "action_by_employee_id")
    private User actionBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date actionDate;

    @Column(length = 1000)
    private String comments;

    // Constructors
    public TicketHistory() {
    }

    public TicketHistory(Ticket ticket, String action, User actionBy, Date actionDate, String comments) {
        this.ticket = ticket;
        this.action = action;
        this.actionBy = actionBy;
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

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public User getActionBy() {
        return actionBy;
    }

    public void setActionBy(User actionBy) {
        this.actionBy = actionBy;
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

    /**
     * This method is called by JPA before the entity is persisted (saved).
     * It sets the actionDate to the current time.
     */
    @PrePersist
    protected void onCreate() {
        actionDate = new Date();
    }

    /**
     * This method is called by JPA before the entity is updated.
     * It updates the actionDate to the current time.  This ensures that the
     * actionDate reflects the last time the TicketHistory was modified.
     */
    @PreUpdate
    protected void onUpdate() {
        actionDate = new Date();
    }
}