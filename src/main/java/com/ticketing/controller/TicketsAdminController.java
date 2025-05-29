package com.ticketing.controller;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;


import com.ticketing.domain.Ticket;
import com.ticketing.domain.TicketHistory;
import com.ticketing.domain.User;
import com.ticketing.dto.TicketDto;
import com.ticketing.service.EmailService;
import com.ticketing.service.TicketHistoryService;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;
import com.ticketing.service.PdfGeneratorService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Controller

@RequestMapping("/api/tickets/admin")
public class TicketsAdminController {
	
	@Value("${spring.mail.username}")
    private String senderEmail;

    private final UserService userService;
    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService; 

    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager

    public TicketsAdminController(UserService userService, TicketService ticketService, TicketHistoryService ticketHistoryService, 
    		PdfGeneratorService pdfGeneratorService, EmailService emailService, 
    		EntityManager entityManager) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.entityManager = entityManager;
        this.emailService = emailService;
    }
    
    // Get all Administrator tickets
    @GetMapping("/{userId}")
    public ResponseEntity<List<TicketDto>> getAdminTickets(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Fetch all tickets
            List<Ticket> allTickets = ticketService.findAll();

            // Filter the tickets based on the criteria
            List<Ticket> filteredTickets = allTickets.stream()
                    .filter(ticket -> 
                            ("APPROVED".equals(ticket.getStatus()) || 
                            ("ASSIGNED".equals(ticket.getStatus()) && 
                             ticket.getAssignee() != null && 
                             ticket.getAssignee().getUserId().equals(userId)))
                    )
                    .collect(Collectors.toList());

            List<TicketDto> ticketDtos = filteredTickets.stream()
                    .map(ticket -> new TicketDto(
                            ticket.getTicketId(),
                            ticket.getTitle(),
                            ticket.getDescription(),
                            ticket.getCreatedBy().getUserId(),
                            ticket.getCreatedBy().getUserName(),
                            ticket.getAssignee() != null ? ticket.getAssignee().getUserId() : null,
                            ticket.getAssignee() != null ? ticket.getAssignee().getUserName() : null,
                            ticket.getPriority(),
                            ticket.getStatus(),
                            ticket.getCreationDate(),
                            ticket.getCategory()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ticketDtos);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get Ticket data
    @GetMapping("ticket/{ticketId}")
    public ResponseEntity<TicketDto> getTicketDto(@PathVariable Long ticketId) {
        Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();

            // Get Comments for the Current Ticket, avoiding duplicates
            List<TicketHistory> ticketHistory = ticket.getHistory();
            Set<String> uniqueComments = new HashSet<>();
            List<String> allComments = new ArrayList<>();

            for (TicketHistory history : ticketHistory) {
                if (history.getTicket().getTicketId().equals(ticketId) && // Ensure it's for the correct ticket
                    history.getComments() != null && !history.getComments().isEmpty()) {
                    String comment = history.getComments();
                    if (uniqueComments.add(comment)) { // Add to set; returns true if new
                        allComments.add(comment);     // ...and add to the list if it's a new comment
                    }
                }
            }

            // Get Attachment List

            allComments.forEach(System.out::println);

            TicketDto ticketDto = new TicketDto(
                    ticket.getTicketId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getCreatedBy().getUserId(),
                    ticket.getCreatedBy().getUserName(),
                    ticket.getAssignee() != null ? ticket.getAssignee().getUserId() : null,
                    ticket.getAssignee() != null ? ticket.getAssignee().getUserName() : null,
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getCreationDate(),
                    ticket.getCategory(),
                    allComments, // Include all comments in the DTO
                    ticket.getAttachments()
            );

            return ResponseEntity.ok(ticketDto);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    
    // update Administrator ticket
    @PostMapping("/update/{userId}/ticket/{ticketId}")
    @Transactional
    public ResponseEntity<?> updateTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @RequestParam("comments") String comments) {
        try {
            Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
            if (!ticketOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Ticket not found with ID: " + ticketId, null));
            }
            Ticket existingTicket = ticketOptional.get();

            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found with ID: " + userId, null));
            }
            User updatedByUser = entityManager.merge(userOptional.get());

            // Update the ticket status
            existingTicket.setStatus("RESOLVED");
            existingTicket.setAssignee(updatedByUser);
            Ticket updatedTicket = ticketService.save(existingTicket);
            
            
            // Create a new TicketHistory record
            TicketHistory historyAssignedStatus = new TicketHistory();
            historyAssignedStatus.setAction("ASSIGNED"); // Use the provided status as the action
            historyAssignedStatus.setTicket(updatedTicket);
            historyAssignedStatus.setActionBy(updatedByUser); //  userId as action_by_employee_id
            ticketHistoryService.save(historyAssignedStatus);

            // Create a new TicketHistory record
            TicketHistory historyResolved = new TicketHistory();
            historyResolved.setAction("RESOLVED"); // Use the provided status as the action
            historyResolved.setComments(comments);
            historyResolved.setTicket(updatedTicket);
            historyResolved.setActionBy(updatedByUser); //  userId as action_by_employee_id
            ticketHistoryService.save(historyResolved);

            List<TicketHistory> historyList = updatedTicket.getHistory();
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
            historyList.add(historyAssignedStatus);
            historyList.add(historyResolved);
            updatedTicket.setHistory(historyList);
            ticketService.save(updatedTicket);
            
            String pdfFilePath = pdfGeneratorService.generateResolutionPdf(updatedTicket, comments);
            
         // 8. --- Send Email Notification via JMS ---
            String recipientEmail = updatedByUser.getEmail(); // Assuming User entity has an email field (changed from updatedByUser for clarity in ticket creation context)
            String emailSubject = "Your Ticket Have Been Resolved: " + updatedTicket.getTitle(); // Changed from existingTicket for clarity in ticket creation context
            String emailBody = String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>New Ticket Created Notification</title>" + 
                    "    <style>" +
                    "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                    "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                    "        .header { background-color: #28a745; padding: 20px; text-align: center; color: #ffffff; }" +
                    "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
                    "        .content h2 { color: #007bff; margin-top: 0; }" +
                    "        .ticket-details { background-color: #e9ecef; padding: 20px; border-radius: 5px; margin-top: 20px; border: 1px solid #dee2e6; }" +
                    "        .ticket-details p { margin: 5px 0; }" +
                    "        .ticket-details strong { color: #0056b3; }" +
                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 0.9em; border-top: 1px solid #e9ecef; }" +
                    "        a { color: #007bff; text-decoration: none; }" +
                    "        a:hover { text-decoration: underline; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='email-container'>" +
                    "        <div class='header'>" +
                    "            <h1>Your Ticket Have Been Resolved</h1>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <p>Dear %s,</p>" +
                    "            <p>A new ticket has been created with the following details:</p>" +
                    "            <div class='ticket-details'>" +
                    "                <p><strong>Ticket ID:</strong> %d</p>" +
                    "                <p><strong>Title:</strong> %s</p>" +
                    "                <p><strong>Description:</strong> %s</p>" +
                    "                <p><strong>Priority:</strong> %s</p>" +
                    "                <p><strong>Status:</strong> %s</p>" +
                    "                <p><strong>Category:</strong> %s</p>" +
                    "            </div>" +
                    "            <p>Thank you for your attention to this matter.</p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>&copy; %d Your Company Name. All rights reserved.</p>" + 
                    "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>",
                    updatedByUser.getUserName(), 
                    updatedTicket.getTicketId(),     
                    updatedTicket.getTitle(),
                    updatedTicket.getDescription(),
                    updatedTicket.getPriority(),
                    updatedTicket.getStatus(),
                    updatedTicket.getCategory(),
                    java.time.Year.now().getValue()
            );

            emailService.sendEmailAsync(recipientEmail, emailSubject, emailBody, senderEmail, pdfFilePath);
            
            pdfGeneratorService.deletePdfFile(pdfFilePath);
            
            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    }
}