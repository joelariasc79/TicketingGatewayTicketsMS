package com.ticketing.controller;


//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketing.dto.TicketEmailInfo;

import com.ticketing.service.EmailService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


// Producer: used by CRON
@Controller
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService; 

    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager
    
    @Value("${spring.mail.username}")
    private String senderEmail;    
    
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    // Endpoint for Quartz job to trigger email sending
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody TicketEmailInfo emailInfo) {
        try {
            emailService.sendEmailAsync(emailInfo.getRecipientEmail(), emailInfo.getSubject(), emailInfo.getBody(), senderEmail);
            return ResponseEntity.ok("Email triggered successfully for " + emailInfo.getRecipientEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email: " + e.getMessage());
        }
    }

   
}