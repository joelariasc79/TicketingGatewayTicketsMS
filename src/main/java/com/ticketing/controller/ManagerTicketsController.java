package com.ticketing.controller;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.ticketing.domain.Attachment;
import com.ticketing.domain.Ticket;
//import com.ticketing.domain.
import com.ticketing.domain.TicketHistory;
import com.ticketing.domain.User;
import com.ticketing.dto.TicketDto;
import com.ticketing.service.AttachmentService;
import com.ticketing.service.TicketHistoryService;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/api/tickets/managers")
public class ManagerTicketsController {

    private final UserService userService;
    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final AttachmentService attachmentService;

    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager

    
    public ManagerTicketsController(UserService userService, TicketService ticketService, TicketHistoryService ticketHistoryService, AttachmentService attachmentService, EntityManager entityManager) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.attachmentService = attachmentService;
        this.entityManager = entityManager;
    }
    
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
    
    @GetMapping("ticket/{ticketId}")
    public ResponseEntity<TicketDto> getTicketDto(@PathVariable Long ticketId) {
        Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();

            // Get Latest Comment
            List<TicketHistory> ticketHistory = ticket.getHistory(); // Replace getTicketHistories() with the actual getter

            TicketHistory lastComment = ticketHistory.stream()
                    .filter(h -> h.getComments() != null && !h.getComments().isEmpty()) // Filter for non-empty comments
                    .max(Comparator.comparing(TicketHistory::getActionDate)) // Find the latest based on creation date
                    .orElse(null);

            String lastCommentText = (lastComment != null) ? lastComment.getComments() : null;
            
            
            // Get Attachment List            

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
                    lastCommentText, // Include the last comment in the DTO
                    ticket.getAttachments()
            );

            return ResponseEntity.ok(ticketDto);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
     
   
    
    @PostMapping("/update/{userId}/{ticketId}")
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

            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    }

   
}