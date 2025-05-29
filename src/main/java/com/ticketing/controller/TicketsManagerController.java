package com.ticketing.controller;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/api/tickets/managers")
public class TicketsManagerController {

    private final UserService userService;
    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final EmailService emailService; 

    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    public TicketsManagerController(UserService userService, TicketService ticketService, 
    		TicketHistoryService ticketHistoryService, 
    		EntityManager entityManager, EmailService emailService) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.entityManager = entityManager;
        this.emailService = emailService;
    }

    
    // List of Manager's Tickets
    @GetMapping("/{userId}/managedTickets")
    public ResponseEntity<List<TicketDto>> getManagedTickets(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User manager = userOptional.get();
            List<Ticket> tickets = ticketService.findTicketsByManager(manager); // Use the service method
            
            List<Ticket> approvedTickets = tickets.stream() 
            	.filter(ticket -> ticket.getStatus().equals("PENDING_APPROVAL"))
//            .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING_APPROVAL)
            .collect(Collectors.toList());
            
            
            List<TicketDto> ticketDtos = approvedTickets.stream()
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
    
    // Ticket HTML
    @GetMapping("/{ticketId}")
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
     
   
    
    @PostMapping("/update/{userId}/ticket/{ticketId}")
    @Transactional
    public ResponseEntity<?> updateTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @RequestParam("status") String status,
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
            existingTicket.setStatus(status);
            Ticket updatedTicket = ticketService.save(existingTicket);

            // Create a new TicketHistory record
            TicketHistory history = new TicketHistory();
            history.setAction(status); // Use the provided status as the action
            history.setComments(comments);
            history.setTicket(updatedTicket);
            history.setActionBy(updatedByUser); //  userId as action_by_employee_id
            ticketHistoryService.save(history);

            List<TicketHistory> historyList = updatedTicket.getHistory();
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
            historyList.add(history);
            updatedTicket.setHistory(historyList);
            ticketService.save(updatedTicket);
    
            // Email Service:
            // String companyLogoUrl = "https://your-company.com/path/to/your/logo.png";
            String emailSubject = "";
            String emailBody = "";
            String recipientEmail = updatedByUser.getEmail(); // Assuming User entity has an email field

            switch(status) {
                case "APPROVED":
                    emailSubject = "The following Ticket has been Approved: " + existingTicket.getTitle();
                    emailBody = String.format(
                            "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Ticket Approved Notification</title>" +
                            "    <style>" +
                            "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                            "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                            "        .header { background-color: #007bff; padding: 20px; text-align: center; color: #ffffff; }" +
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
                            "            <h1>Ticket Notification</h1>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <p>Dear %s,</p>" +
                            "            <p>The following ticket has been <strong>Approved</strong>:</p>" +
                            "            <div class='ticket-details'>" +
                            "                <p><strong>Ticket ID:</strong> %d</p>" +
                            "                <p><strong>Title:</strong> %s</p>" +
                            "                <p><strong>Description:</strong> %s</p>" +
                            "                <p><strong>Priority:</strong> %s</p>" +
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
                            existingTicket.getTicketId(),
                            existingTicket.getTitle(),
                            existingTicket.getDescription(),
                            existingTicket.getPriority(),
                            existingTicket.getCategory(),
                            java.time.Year.now().getValue()
                        );
                    break;
                case "REJECTED":
                    emailSubject = "The following Ticket has been Rejected: " + existingTicket.getTitle();
                    emailBody = String.format(
                            "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Ticket Rejected Notification</title>" +
                            "    <style>" +
                            "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                            "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                            "        .header { background-color: #dc3545; padding: 20px; text-align: center; color: #ffffff; }" +
                            "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
                            "        .content h2 { color: #007bff; margin-top: 0; }" +
                            "        .ticket-details { background-color: #e9ecef; padding: 20px; border-radius: 5px; margin-top: 20px; border: 1px solid #dee2e6; }" +
                            "        .ticket-details p { margin: 5px 0; }" +
                            "        .ticket-details strong { color: #dc3545; }" +
                            "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 0.9em; border-top: 1px solid #e9ecef; }" +
                            "        a { color: #007bff; text-decoration: none; }" +
                            "        a:hover { text-decoration: underline; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='email-container'>" +
                            "        <div class='header'>" +
                            "            <h1>Ticket Notification</h1>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <p>Dear %s,</p>" +
                            "            <p>The following ticket has been <strong>Rejected</strong>:</p>" +
                            "            <div class='ticket-details'>" +
                            "                <p><strong>Ticket ID:</strong> %d</p>" +
                            "                <p><strong>Title:</strong> %s</p>" +
                            "                <p><strong>Description:</strong> %s</p>" +
                            "                <p><strong>Priority:</strong> %s</p>" +
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
                            existingTicket.getTicketId(),
                            existingTicket.getTitle(),
                            existingTicket.getDescription(),
                            existingTicket.getPriority(),
                            existingTicket.getCategory(),
                            java.time.Year.now().getValue()
                        );
                    break;
            }

            // Pass the senderEmail as the fourth argument
            emailService.sendEmailAsync(recipientEmail, emailSubject, emailBody, senderEmail);

            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    }

   
}