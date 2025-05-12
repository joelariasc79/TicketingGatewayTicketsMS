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
import java.util.List;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by_employee_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "assignee_employee_id")
    private User assignee;

    private String priority; // LOW, MEDIUM, HIGH

    private String status; // OPEN, PENDING_APPROVAL, APPROVED, REJECTED, ASSIGNED, RESOLVED, CLOSED, REOPENED

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    private String category;

    private String fileAttachmentPath;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistory> history;

    // Constructors
    public Ticket() {
    }

    public Ticket(String title, String description, User createdBy, String priority, String status, Date creationDate, String category) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.priority = priority;
        this.status = status;
        this.creationDate = creationDate;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
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

    public String getFileAttachmentPath() {
        return fileAttachmentPath;
    }

    public void setFileAttachmentPath(String fileAttachmentPath) {
        this.fileAttachmentPath = fileAttachmentPath;
    }

    public List<TicketHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TicketHistory> history) {
        this.history = history;
    }
}