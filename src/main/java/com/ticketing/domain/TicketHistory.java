package com.ticketing.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.*;
import java.util.Date;

@Entity
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}