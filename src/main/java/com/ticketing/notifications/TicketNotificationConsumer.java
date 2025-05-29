package com.ticketing.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.dto.messages.EmailMessage;
import com.ticketing.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.List; // Import List

// Consumer
@Component
public class TicketNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Value("${app.jms.ticket-email-queue}")
    private String ticketEmailQueue; // You can remove this field if not directly used, but it's fine here.

    // Optional: for the base directory where PDFs are stored for attachments
    // Note: If you're embedding bytes directly, this might not be needed for sending.
    // It would be for PRODUCING the message if you read files from disk.
//    @Value("${app.pdf.storage-base-dir}")
//    private String pdfStorageBaseDir; // Keep this if you plan to use it for *reading* files before enqueuing

    @Autowired
    public TicketNotificationConsumer(ObjectMapper objectMapper, EmailService emailService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }
    
    @JmsListener(destination = "${app.jms.ticket-email-queue}", containerFactory = "jmsListenerContainerFactory")
    public void ticketEmailNotification(EmailMessage msg) {
        try {
            System.out.println("Consumed: Ticket Notification - " + msg.toString());
            String subject = msg.getSubject();
            String body = msg.getBody();
            String userEmail = msg.getTo();
            String senderEmail = msg.getSender();
            List<EmailMessage.Attachment> attachments = msg.getAttachments(); // Get attachments from the message

            if (attachments != null && !attachments.isEmpty()) {
                // If there are attachments, use the new method in EmailService
                emailService.sendEmailWithAttachments(userEmail, subject, body, senderEmail, attachments);
            } else {
                // Otherwise, use the existing sendHtmlEmail method
                emailService.sendHtmlEmail(userEmail, subject, body, senderEmail);
            }

        } catch (Exception e) {
            System.err.println("Error processing ticket notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @JmsListener(destination = "${app.jms.ticket-email-attachment-queue}", containerFactory = "jmsListenerContainerFactory")
    public void ticketEmailAttachmentNotification(EmailMessage msg) {
        try {
            System.out.println("Consumed: Ticket Notification (Attachment Queue) - " + msg.toString());
            String subject = msg.getSubject();
            String body = msg.getBody();
            String userEmail = msg.getTo();
            String senderEmail = msg.getSender();
            List<EmailMessage.Attachment> attachments = msg.getAttachments(); // Attachments are expected here

            if (attachments != null && !attachments.isEmpty()) {
                emailService.sendEmailWithAttachments(userEmail, subject, body, senderEmail, attachments);
            } else {
                System.err.println("Warning: Received message on attachment queue without any attachments for " + userEmail);
                // Optionally, fall back to sending a plain email or throw an error
                emailService.sendHtmlEmail(userEmail, subject, body, senderEmail);
            }

        } catch (Exception e) {
            System.err.println("Error processing ticket attachment notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}