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
@RequestMapping("/api/tickets/users")
public class UserTicketsController {

    private final UserService userService;
    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final AttachmentService attachmentService;

    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    
    public UserTicketsController(UserService userService, TicketService ticketService, TicketHistoryService ticketHistoryService, AttachmentService attachmentService, EntityManager entityManager) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.attachmentService = attachmentService;
        this.entityManager = entityManager;
    }
    
    @GetMapping("/{userId}/ticketList")
    public ResponseEntity<List<TicketDto>> getUserTicketsDto(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Ticket> tickets = ticketService.findByCreatedBy(userId);
            
            List<TicketDto> ticketDtos = tickets.stream()
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
                    ticket.getCategory()
            );

            return ResponseEntity.ok(ticketDto);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
   
    
    @PostMapping("/create/{userId}")
    @Transactional
    public ResponseEntity<?> createTicket(@PathVariable Long userId,
                                         @RequestParam("title") String title,
                                         @RequestParam("description") String description,
                                         @RequestParam("priority") String priority,
                                         @RequestParam("category") String category,
                                         @RequestParam("comments") String comments,
                                         @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            // 1. Fetch the User
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found with ID: " + userId, null));
            }
            // 2. Use entityManager.merge() to ensure the User is managed within the current transaction
            User createdByUser = entityManager.merge(userOptional.get());

            // 3. Create a new Ticket object
            Ticket newTicket = new Ticket();
            newTicket.setCreatedBy(createdByUser);
            newTicket.setTitle(title);
            newTicket.setDescription(description);
            newTicket.setPriority(priority);
            newTicket.setStatus("OPEN"); // Set status to OPEN
            newTicket.setCategory(category);

            // 4. Save the new Ticket to generate its ID
            Ticket savedTicket = ticketService.save(newTicket);
            Long ticket_id = savedTicket.getTicketId();

            // 5. Handle file uploads and create Attachment entities
            List<Attachment> attachments = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = file.getOriginalFilename() + "_" + ticket_id;
                        String fileType = file.getContentType();
                        byte[] bytes = file.getBytes();

                        String filePath = Paths.get(uploadDirectory, fileName).toString();
                        Path destinationPath = Paths.get(uploadDirectory, fileName);
                        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                        Attachment attachment = new Attachment();
                        attachment.setFileName(fileName);
                        attachment.setFileType(fileType);
                        attachment.setData(bytes);
                        attachment.setFilePath(filePath);
                        attachment.setTicket(savedTicket);
                        attachmentService.save(attachment);
                        attachments.add(attachment);
                    }
                }
            }
            savedTicket.setAttachments(attachments);
            ticketService.save(savedTicket); // Save the ticket again to associate attachments

            // 6. Create a TicketHistory record
            TicketHistory createdHistory = new TicketHistory();
            createdHistory.setAction("CREATED");
            createdHistory.setComments(comments);
            createdHistory.setTicket(savedTicket);
            createdHistory.setActionBy(createdByUser);
            ticketHistoryService.save(createdHistory);

            // 7. Associate history with the ticket
            List<TicketHistory> historyList = new ArrayList<>();
            historyList.add(createdHistory);
            savedTicket.setHistory(historyList);
            ticketService.save(savedTicket); // Save again to associate history

            // 8. Return a success response with the new ticket ID
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Ticket created successfully", savedTicket.getTicketId()));

        } catch (IOException e) { // Catch IOException for file operations
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to create ticket: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/update/{userId}/{ticketId}")
    @Transactional
    public ResponseEntity<?> updateTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestParam("category") String category,
            @RequestParam("comments") String comments,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
            if (!ticketOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Ticket not found with ID: " + ticketId, null));
            }
            Ticket existingTicket = ticketOptional.get();
            String currentStatus = existingTicket.getStatus();

            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found with ID: " + userId, null));
            }
            User updatedByUser = entityManager.merge(userOptional.get());

            String newStatus = currentStatus;
            if ("RESOLVED".equalsIgnoreCase(currentStatus)) {
                newStatus = "REOPENED";
            } else if ("REOPENED".equalsIgnoreCase(currentStatus)) {
                newStatus = "REOPENED";
            } else if ("REJECTED".equalsIgnoreCase(currentStatus) || "CLOSED".equalsIgnoreCase(currentStatus)) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Cannot update a " + currentStatus + " ticket", null));
            } else if ("OPEN".equalsIgnoreCase(currentStatus) || "PENDING_APPROVAL".equalsIgnoreCase(currentStatus)
                    || "APPROVED".equalsIgnoreCase(currentStatus) || "ASSIGNED".equalsIgnoreCase(currentStatus)) {
                newStatus = "OPEN";
            }

            existingTicket.setTitle(title);
            existingTicket.setDescription(description);
            existingTicket.setPriority(priority);
            existingTicket.setStatus(newStatus);
            existingTicket.setCategory(category);

            Ticket updatedTicket = ticketService.save(existingTicket);

            if (!("REJECTED".equalsIgnoreCase(currentStatus) || "CLOSED".equalsIgnoreCase(currentStatus))) {
                if (files != null && files.length > 0) {
                    Ticket mergedTicket = entityManager.merge(updatedTicket);

                    if (mergedTicket.getAttachments() != null) {
                        mergedTicket.getAttachments().clear();
                    }

                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String fileName = file.getOriginalFilename() + "_" + ticketId;
                            String fileType = file.getContentType();
                            byte[] bytes = file.getBytes();
                            String filePath = Paths.get(uploadDirectory, fileName).toString();
                            Path destinationPath = Paths.get(uploadDirectory, fileName);
                            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                            Attachment attachment = new Attachment();
                            attachment.setFileName(fileName);
                            attachment.setFileType(fileType);
                            attachment.setData(bytes);
                            attachment.setFilePath(filePath);
                            attachment.setTicket(mergedTicket);
                            mergedTicket.getAttachments().add(attachment);
                        }
                    }
                    ticketService.save(mergedTicket);
                }
            }

            if (!"REJECTED".equalsIgnoreCase(currentStatus)) {
                TicketHistory history = new TicketHistory();
                if ("RESOLVED".equalsIgnoreCase(currentStatus) && "REOPENED".equalsIgnoreCase(newStatus)) {
                    history.setAction("REOPENED");
                } else {
                    history.setAction("UPDATED");
                }

                history.setComments(comments);
                history.setTicket(updatedTicket);
                history.setActionBy(updatedByUser);
                ticketHistoryService.save(history);

                List<TicketHistory> historyList = updatedTicket.getHistory();
                if (historyList == null) {
                    historyList = new ArrayList<>();
                }
                historyList.add(history);
                updatedTicket.setHistory(historyList);
                ticketService.save(updatedTicket);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    }
    
    
    @PostMapping("/send/create/{userId}")
    @Transactional
    public ResponseEntity<?> sendCreateTicket(@PathVariable Long userId,
                                         @RequestParam("title") String title,
                                         @RequestParam("description") String description,
                                         @RequestParam("priority") String priority,
                                         @RequestParam("category") String category,
                                         @RequestParam("comments") String comments,
                                         @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            // 1. Fetch the User
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found with ID: " + userId, null));
            }
            // 2. Use entityManager.merge() to ensure the User is managed within the current transaction
            User createdByUser = entityManager.merge(userOptional.get());

            // 3. Create a new Ticket object
            Ticket newTicket = new Ticket();
            newTicket.setCreatedBy(createdByUser);
            newTicket.setTitle(title);
            newTicket.setDescription(description);
            newTicket.setPriority(priority);
            newTicket.setStatus("PENDING_APPROVAL"); // Set status to OPEN
            newTicket.setCategory(category);

            // 4. Save the new Ticket to generate its ID
            Ticket savedTicket = ticketService.save(newTicket);
            Long ticket_id = savedTicket.getTicketId();

            // 5. Handle file uploads and create Attachment entities
            List<Attachment> attachments = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = file.getOriginalFilename() + "_" + ticket_id;
                        String fileType = file.getContentType();
                        byte[] bytes = file.getBytes();

                        String filePath = Paths.get(uploadDirectory, fileName).toString();
                        Path destinationPath = Paths.get(uploadDirectory, fileName);
                        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                        Attachment attachment = new Attachment();
                        attachment.setFileName(fileName);
                        attachment.setFileType(fileType);
                        attachment.setData(bytes);
                        attachment.setFilePath(filePath);
                        attachment.setTicket(savedTicket);
                        attachmentService.save(attachment);
                        attachments.add(attachment);
                    }
                }
            }
            savedTicket.setAttachments(attachments);
            ticketService.save(savedTicket); // Save the ticket again to associate attachments

            // 6. Create a TicketHistory record
            TicketHistory createdHistory = new TicketHistory();
            createdHistory.setAction("CREATED");
            createdHistory.setComments(comments);
            createdHistory.setTicket(savedTicket);
            createdHistory.setActionBy(createdByUser);
            ticketHistoryService.save(createdHistory);

            // 7. Associate history with the ticket
            List<TicketHistory> historyList = new ArrayList<>();
            historyList.add(createdHistory);
            savedTicket.setHistory(historyList);
            ticketService.save(savedTicket); // Save again to associate history

            // 8. Return a success response with the new ticket ID
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Ticket created successfully", savedTicket.getTicketId()));

        } catch (IOException e) { // Catch IOException for file operations
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to create ticket: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/send/update/{userId}/{ticketId}")
    @Transactional
    public ResponseEntity<?> sendUpdateTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestParam("category") String category,
            @RequestParam("comments") String comments,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
            if (!ticketOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Ticket not found with ID: " + ticketId, null));
            }
            Ticket existingTicket = ticketOptional.get();
            String currentStatus = existingTicket.getStatus();

            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found with ID: " + userId, null));
            }
            User updatedByUser = entityManager.merge(userOptional.get());

            String newStatus = currentStatus;
            if ("RESOLVED".equalsIgnoreCase(currentStatus)) {
                newStatus = "PENDING_APPROVAL";
            } else if ("REOPENED".equalsIgnoreCase(currentStatus)) {
                newStatus = "PENDING_APPROVAL";
            } else if ("REJECTED".equalsIgnoreCase(currentStatus) || "CLOSED".equalsIgnoreCase(currentStatus)) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Cannot update a " + currentStatus + " ticket", null));
            } else if ("OPEN".equalsIgnoreCase(currentStatus) || "PENDING_APPROVAL".equalsIgnoreCase(currentStatus)
                    || "APPROVED".equalsIgnoreCase(currentStatus) || "ASSIGNED".equalsIgnoreCase(currentStatus)) {
                newStatus = "PENDING_APPROVAL";
            }

            existingTicket.setTitle(title);
            existingTicket.setDescription(description);
            existingTicket.setPriority(priority);
            existingTicket.setStatus(newStatus);
            existingTicket.setCategory(category);

            Ticket updatedTicket = ticketService.save(existingTicket);

            if (!("REJECTED".equalsIgnoreCase(currentStatus) || "CLOSED".equalsIgnoreCase(currentStatus))) {
                if (files != null && files.length > 0) {
                    Ticket mergedTicket = entityManager.merge(updatedTicket);

                    if (mergedTicket.getAttachments() != null) {
                        mergedTicket.getAttachments().clear();
                    }

                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String fileName = file.getOriginalFilename() + "_" + ticketId;
                            String fileType = file.getContentType();
                            byte[] bytes = file.getBytes();
                            String filePath = Paths.get(uploadDirectory, fileName).toString();
                            Path destinationPath = Paths.get(uploadDirectory, fileName);
                            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                            Attachment attachment = new Attachment();
                            attachment.setFileName(fileName);
                            attachment.setFileType(fileType);
                            attachment.setData(bytes);
                            attachment.setFilePath(filePath);
                            attachment.setTicket(mergedTicket);
                            mergedTicket.getAttachments().add(attachment);
                        }
                    }
                    ticketService.save(mergedTicket);
                }
            }

            if (!"REJECTED".equalsIgnoreCase(currentStatus)) {
                TicketHistory history = new TicketHistory();
                if ("RESOLVED".equalsIgnoreCase(currentStatus) && "REOPENED".equalsIgnoreCase(newStatus)) {
                    history.setAction("REOPENED");
                } else {
                    history.setAction("UPDATED");
                }

                history.setComments(comments);
                history.setTicket(updatedTicket);
                history.setActionBy(updatedByUser);
                ticketHistoryService.save(history);

                List<TicketHistory> historyList = updatedTicket.getHistory();
                if (historyList == null) {
                    historyList = new ArrayList<>();
                }
                historyList.add(history);
                updatedTicket.setHistory(historyList);
                ticketService.save(updatedTicket);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    }


    @GetMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<?> getAttachmentsByTicketId(@PathVariable Long ticketId) {
        List<Attachment> attachments = attachmentService.findByTicketId(ticketId);
        return ResponseEntity.ok(new ApiResponse(true, "Attachments retrieved successfully", attachments));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        Optional<Attachment> attachmentOptional = attachmentService.findById(attachmentId);
        if (attachmentOptional.isPresent()) {
            Attachment attachment = attachmentOptional.get();
            // Optionally delete the physical file from the server as well
            java.io.File fileToDelete = new java.io.File(attachment.getFilePath());
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    attachmentService.delete(attachment);
                    return ResponseEntity.ok(new ApiResponse(true, "Attachment deleted successfully", null));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse(false, "Failed to delete file from server", null));
                }
            } else {
                attachmentService.delete(attachment);
                return ResponseEntity.ok(new ApiResponse(true, "Attachment deleted successfully (file not found on server)", null));
            }
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Attachment not found with ID: " + attachmentId, null));
        }
    }
    
    @GetMapping("/delete/{ticketId}")
    public String deleteTicket(@PathVariable Long ticketId) {
    	User user = ticketService.findCreator(ticketId);
    	Long userId = user.getUserId();
    	
    	ticketService.deleteById(ticketId);
        return "redirect:http://localhost:8181/ticketing/userTicketsList/" + userId;
    }
   
}