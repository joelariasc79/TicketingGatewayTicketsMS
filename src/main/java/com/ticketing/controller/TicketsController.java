package com.ticketing.controller;

import java.util.List;


//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketing.dto.TicketEmailInfo;

import com.ticketing.service.TicketService;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Controller
@RequestMapping("/api/tickets")
public class TicketsController {


    private final TicketService ticketService;


    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager
    
    @Value("${spring.mail.username}")
    private String senderEmail;    
    
    public TicketsController(TicketService ticketService) {
        this.ticketService = ticketService;

    }
    
    // Endpoint for Quartz job to fetch unupdated resolved-rejected tickets
    @GetMapping("/close-resolved-rejected-unupdated-for-n-days/{days}")
    public ResponseEntity<List<TicketEmailInfo>> getTicketsResolvedAdnRejectedUnupdatedForDays(@PathVariable int days) {
        List<TicketEmailInfo> tickets = ticketService.getTicketsResolvedAdnRejectedUnupdatedForDays(days);
        return ResponseEntity.ok(tickets);
    }
    
    // Endpoint for Quartz job to fetch unupdated pending_approval tickets
    @GetMapping("/pending-approval-unupdated-for-n-days/{days}")
    public ResponseEntity<List<TicketEmailInfo>> getTicketsPendingApprovalUnupdatedForDays(@PathVariable int days) {
    	System.out.println("Pending before");
        List<TicketEmailInfo> tickets = ticketService.getTicketsPendingApprovalUnupdatedForDays(days);
        System.out.println("Pending after");
        return ResponseEntity.ok(tickets);
    }
    
 // Endpoint for Quartz job to fetch unupdated pending_approval tickets
    @GetMapping("/assigned-unupdated-for-n-days/{days}")
    public ResponseEntity<List<TicketEmailInfo>> getTicketsAssigedUnupdatedForDays(@PathVariable int days) {
        List<TicketEmailInfo> tickets = ticketService.getTicketsAssigedUnupdatedForDays(days);
        return ResponseEntity.ok(tickets);
    }

    // Endpoint for close tickets
    @PostMapping("/close/{days}")
    public ResponseEntity<String> sendEmail(@RequestBody TicketEmailInfo emailInfo, @PathVariable int days) {
        try {
            Long ticketId = emailInfo.getTicketId();
            ticketService.closeById(ticketId, days);
            return ResponseEntity.ok("Email triggered successfully for " + emailInfo.getRecipientEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email: " + e.getMessage());
        }
    }

   
}