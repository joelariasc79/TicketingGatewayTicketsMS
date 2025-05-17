package com.ticketing.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @JsonBackReference("createdTickets") // Add this, use a unique name
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "assignee_user_id")
    @JsonBackReference("assignedTickets") // Add this, use a unique name
    private User assignee;

    private String priority; // LOW, MEDIUM, HIGH

    private String status; // OPEN, PENDING_APPROVAL, APPROVED, REJECTED, ASSIGNED, RESOLVED, CLOSED, REOPENED

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    private String category;


    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("history")
    private List<TicketHistory> history;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("attachments")
    private List<Attachment> attachments; // Add this line

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

    public List<TicketHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TicketHistory> history) {
        this.history = history;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * This method is called by JPA before the entity is persisted (saved).
     * It sets the creationDate to the current time.
     */
    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    /**
     * This method is called by JPA before the entity is updated.
     * It updates the creationDate to the current time.
     * Note:  In most cases, you would *not* want to update the creationDate.
     * This is included for completeness, but should be used with caution.
     */
    @PreUpdate
    protected void onUpdate() {
        creationDate = new Date();
    }
}