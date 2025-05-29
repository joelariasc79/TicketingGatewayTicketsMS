package com.ticketing.dto;

public class TicketEmailInfo {
	private Long ticketId;
	private String recipientEmail;
	private String subject;
	private String body;

 
	// Getters and Setters
	public Long getTicketId() {return ticketId;}
	public void setTicketId(Long ticketId) {this.ticketId = ticketId;}
	public String getRecipientEmail() { return recipientEmail; }
	public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public String getBody() { return body; }
	public void setBody(String body) { this.body = body; }
	

	 public TicketEmailInfo() {
		super();
	}
	 
	public TicketEmailInfo(Long ticketId, String recipientEmail, String subject, String body) {
		super();
		this.ticketId=ticketId;
		this.recipientEmail = recipientEmail;
		this.subject = subject;
		this.body = body;
	}
	@Override
	 public String toString() {
	     return "TicketEmailInfo{" +
	    		"ticketId='" + ticketId + '\'' +
	            "recipientEmail='" + recipientEmail + '\'' +
	            ", subject='" + subject + '\'' +
	            ", body='" + body.substring(0, Math.min(body.length(), 50)) + "...'"+ // Truncate body for logging
	            '}';
	 }
	
}