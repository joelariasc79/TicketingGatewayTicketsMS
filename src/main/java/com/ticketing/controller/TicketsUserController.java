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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Date;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import com.ticketing.domain.Attachment;
import com.ticketing.domain.Ticket;
import com.ticketing.domain.TicketHistory;
import com.ticketing.domain.User;
import com.ticketing.dto.TicketDto;
import com.ticketing.service.AttachmentService;
import com.ticketing.service.TicketHistoryService;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;
import com.ticketing.service.EmailService; 
import com.ticketing.util.FileUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/tickets/users")
public class TicketsUserController {

    private final UserService userService;
    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final AttachmentService attachmentService;
    private final EmailService emailService; 
    private final WebClient webClient;
    
    private static byte REOPENCLOSEBUTTONDAYSENABLED = 7;
    
    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager

    @Value("${file.upload.directory}")
    private String uploadDirectory;
    
    @Value("${spring.mail.username}")
    private String senderEmail;

    public TicketsUserController(UserService userService, TicketService ticketService, TicketHistoryService ticketHistoryService, 
    		AttachmentService attachmentService, EntityManager entityManager, EmailService emailService,
            WebClient notificationServiceWebClient) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.attachmentService = attachmentService;
        this.entityManager = entityManager;
        this.emailService = emailService;
        this.webClient = notificationServiceWebClient;
    }
    
    // Ticket List HTML
    
    @GetMapping("/{userId}/ticketList")
    public ResponseEntity<List<TicketDto>> getUserTicketsDto(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Ensure that findByCreatedBy loads history if it's lazy-loaded,
            // or fetch tickets with their history eagerly in your TicketService.
            List<Ticket> tickets = ticketService.findByCreatedBy(userId); 

            Date now = new Date(); // Get current date once

            List<TicketDto> ticketDtos = tickets.stream()
                    .filter(ticket -> !"CLOSED".equals(ticket.getStatus())) // Filter out tickets with status "CLOSED"
                    .map(ticket -> {
                        // --- Calculate latestActionDate and daysSinceLatestAction ---
                        Date latestActionDate = ticket.getCreationDate(); // Default to creation date if no history
                        Long daysSinceLatestAction = 0L; // Default to 0

                        if (ticket.getHistory() != null && !ticket.getHistory().isEmpty()) {
                            Optional<TicketHistory> latestHistoryEntry = ticket.getHistory().stream()
                                    .sorted(Comparator.comparing(TicketHistory::getActionDate).reversed())
                                    .findFirst();

                            if (latestHistoryEntry.isPresent()) {
                                latestActionDate = latestHistoryEntry.get().getActionDate();
                            }
                        }

                        // Calculate days difference
                        if (latestActionDate != null) {
                            long diffInMillis = now.getTime() - latestActionDate.getTime();
                            daysSinceLatestAction = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                        }
                        
                        boolean displayReopenAndCloseButton = false;
                        if (daysSinceLatestAction <= REOPENCLOSEBUTTONDAYSENABLED) 
                        	displayReopenAndCloseButton = true;
                        
                        // --- End Calculation ---

                        return new TicketDto(
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
                                latestActionDate,
                                daysSinceLatestAction,
                                displayReopenAndCloseButton
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ticketDtos);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    

    
    @GetMapping("/{userId}/closedTicketList")
    public ResponseEntity<List<TicketDto>> getUserClosedTicketsDto(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            // Ensure that findByCreatedBy loads history if it's lazy-loaded,
            // or fetch tickets with their history eagerly in your TicketService.
            List<Ticket> tickets = ticketService.findByCreatedBy(userId);

            List<TicketDto> ticketDtos = tickets.stream()
                    .filter(ticket -> "CLOSED".equals(ticket.getStatus())) // Filter to include ONLY "CLOSED" tickets
                    .map(ticket -> {
                        // --- Find ClosedBy User Details ---
                        Long closedByUserId = null;
                        String closedByUserName = null;
                        if (ticket.getHistory() != null && !ticket.getHistory().isEmpty()) {
                            // Find the LATEST history entry with action "CLOSED"
                            Optional<TicketHistory> latestClosedAction = ticket.getHistory().stream()
                                .filter(history -> "CLOSED".equals(history.getAction()))
                                .sorted(Comparator.comparing(TicketHistory::getActionDate).reversed())
                                .findFirst();

                            if (latestClosedAction.isPresent()) {
                                TicketHistory closedEntry = latestClosedAction.get();
                                if (closedEntry.getActionBy() != null) { // Ensure the actionBy user is not null
                                    closedByUserId = closedEntry.getActionBy().getUserId();
                                    closedByUserName = closedEntry.getActionBy().getUserName();
                                }
                            }
                        }
                        // --- End Find ClosedBy User Details ---

                        return new TicketDto(
                                ticket.getTicketId(),
                                ticket.getTitle(),
                                ticket.getDescription(),
                                ticket.getStatus(),
                                closedByUserId,         
                                closedByUserName
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ticketDtos);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    
    @PutMapping("{userId}/tickets/{ticketId}/reopen")
    @Transactional
    public ResponseEntity<?> reopenTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId) {
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

            // --- Logic to get the latest comment from the existing ticket's history ---
            String latestCommentFromHistory = null;
            if (existingTicket.getHistory() != null && !existingTicket.getHistory().isEmpty()) {
                // Sort history by actionDate in descending order to find the latest
                Optional<TicketHistory> latestHistoryEntry = existingTicket.getHistory().stream()
                    .sorted(Comparator.comparing(TicketHistory::getActionDate).reversed())
                    .findFirst();

                if (latestHistoryEntry.isPresent()) {
                    latestCommentFromHistory = latestHistoryEntry.get().getComments();
                }
            }
            // --- End of logic to get latest comment ---


            // Update the ticket status
            existingTicket.setStatus("REOPENED");
            
            Ticket updatedTicket = ticketService.save(existingTicket); // Save the status change

            // Create a new TicketHistory record for the REOPENED action
            TicketHistory historyReopened = new TicketHistory(); // Renamed from historyClosed for clarity
            
            historyReopened.setAction("REOPENED"); // Action is REOPENED
            historyReopened.setTicket(updatedTicket);
            historyReopened.setActionBy(updatedByUser); // User who performed the action
            historyReopened.setComments(latestCommentFromHistory != null ? latestCommentFromHistory : "Ticket reopened."); // Set the latest comment, or a default message
            // You might also want to set oldStatus and newStatus if your TicketHistoryDto supports it
            // historyReopened.setOldStatus(existingTicket.getStatus()); // Assuming existingTicket.getStatus() holds the status BEFORE reopening
            // historyReopened.setNewStatus("REOPENED");

            ticketHistoryService.save(historyReopened); // Save the new history entry

            // Add the new history entry to the ticket's history list
            List<TicketHistory> historyList = updatedTicket.getHistory();
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
            historyList.add(historyReopened);
            updatedTicket.setHistory(historyList);
            ticketService.save(updatedTicket); // Save again to update the history collection on the ticket

            return ResponseEntity.ok(new ApiResponse(true, "Ticket reopened successfully", updatedTicket.getTicketId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to reopen ticket: " + e.getMessage(), null));
        }
    }
    
    
    @PutMapping("{userId}/tickets/{ticketId}/close")
    @Transactional
    public ResponseEntity<?> closeTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId) {
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
            existingTicket.setStatus("CLOSED");

            Ticket updatedTicket = ticketService.save(existingTicket);

            // Create a new TicketHistory record
            TicketHistory historyClosed = new TicketHistory();
            historyClosed.setAction("CLOSED"); // Use the provided status as the action
            historyClosed.setTicket(updatedTicket);
            historyClosed.setActionBy(updatedByUser); //  userId as action_by_employee_id
            ticketHistoryService.save(historyClosed);

            List<TicketHistory> historyList = updatedTicket.getHistory();
            if (historyList == null) {
                historyList = new ArrayList<>();
            }

            historyList.add(historyClosed);
            updatedTicket.setHistory(historyList);
            ticketService.save(updatedTicket);

            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    } 
    
    @DeleteMapping("tickets/{ticketId}/delete") // Changed to @DeleteMapping
    @Transactional // Ensure transactional behavior for delete
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId) {
        try {
            // Optional: Add logic to check if the user is authorized to delete this ticket
            // For example, if it's the creator or an admin
            // User user = ticketService.findCreator(ticketId);
            // Long userId = user.getUserId();

            ticketService.deleteById(ticketId);
            return ResponseEntity.ok(new ApiResponse(true, "Ticket deleted successfully", null));
        } catch (Exception e) {
            System.err.println("Error deleting ticket with ID " + ticketId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse(false, "Failed to delete ticket: " + e.getMessage(), null));
        }
    }
    
    // Ticket HTML
    
    @GetMapping("ticket/{ticketId}")
    public ResponseEntity<TicketDto> getTicketDto(@PathVariable Long ticketId) {
        Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();

            // Logic to find the latest comment
            String latestComment = null;
            if (ticket.getHistory() != null && !ticket.getHistory().isEmpty()) {
                // Sort history by actionDate in descending order to find the latest
                Optional<TicketHistory> latestHistoryEntry = ticket.getHistory().stream()
                    .sorted(Comparator.comparing(TicketHistory::getActionDate).reversed())
                    .findFirst();

                if (latestHistoryEntry.isPresent()) {
                    latestComment = latestHistoryEntry.get().getComments();
                }
            }

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
                    latestComment, // Pass the extracted latestComment here
                    ticket.getAttachments() // Assuming attachments are already loaded or fetched
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
            Long ticketId = savedTicket.getTicketId();

            // 5. Handle file uploads and create Attachment entities
            List<Attachment> attachments = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = FileUtils.getFileNameWithoutExtension(file) +  "_" + ticketId + "." + FileUtils.getFileExtension(file);
                        
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
                        	String fileName = FileUtils.getFileNameWithoutExtension(file) +  "_" + ticketId + "." + FileUtils.getFileExtension(file);
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
    
 // Your existing sendCreateTicket method
//    @PostMapping("/send/create/{userId}")
//    @Transactional // Use org.springframework.transaction.annotation.Transactional
//    public ResponseEntity<?> sendCreateTicket(@PathVariable Long userId,
//                                         @RequestParam("title") String title,
//                                         @RequestParam("description") String description,
//                                         @RequestParam("priority") String priority,
//                                         @RequestParam("category") String category,
//                                         @RequestParam("comments") String comments,
//                                         @RequestParam(value = "files", required = false) MultipartFile[] files) {
//        try {
//            // 1. Fetch the User
//            Optional<User> userOptional = userService.findById(userId);
//            if (!userOptional.isPresent()) {
//                // Ensure ApiResponse is correctly defined or replace with Map/JSON structure
//                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found with ID: " + userId, null));
//            }
//            // 2. Use entityManager.merge() to ensure the User is managed within the current transaction
//            User createdByUser = entityManager.merge(userOptional.get());
//
//            // 3. Create a new Ticket object
//            Ticket newTicket = new Ticket();
//            newTicket.setCreatedBy(createdByUser);
//            newTicket.setTitle(title);
//            newTicket.setDescription(description);
//            newTicket.setPriority(priority);
//            newTicket.setStatus("PENDING_APPROVAL"); // Set status
//            newTicket.setCategory(category);
//
//            // 4. Save the new Ticket to generate its ID
//            Ticket savedTicket = ticketService.save(newTicket);
//            Long ticketId = savedTicket.getTicketId();
//
//            // 5. Handle file uploads and create Attachment entities
//            List<Attachment> attachments = new ArrayList<>();
//            if (files != null && files.length > 0) {
//                for (MultipartFile file : files) {
//                    if (!file.isEmpty()) {
//                        String originalFileName = file.getOriginalFilename();
//                        String fileExtension = FileUtils.getFileExtension(file);
//                        // Ensure unique file name, e.g., appending ticket ID and a timestamp or UUID
//                        String fileName = FileUtils.getFileNameWithoutExtension(file) + "_" + ticketId + "_" + System.currentTimeMillis() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
//                        String fileType = file.getContentType();
//                        byte[] bytes = file.getBytes(); // If you need to store in DB directly
//
//                        String filePath = Paths.get(uploadDirectory, fileName).toString();
//                        Path destinationPath = Paths.get(uploadDirectory, fileName);
//                        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
//
//                        Attachment attachment = new Attachment();
//                        attachment.setFileName(fileName);
//                        attachment.setFileType(fileType);
//                        attachment.setData(bytes); // Consider storing large files on disk, not directly in DB
//                        attachment.setFilePath(filePath); // Store path for disk-based storage
//                        attachment.setTicket(savedTicket);
//                        attachmentService.save(attachment);
//                        attachments.add(attachment);
//                    }
//                }
//            }
//            savedTicket.setAttachments(attachments);
//            ticketService.save(savedTicket); // Save the ticket again to associate attachments
//
//            // 6. Create a TicketHistory record
//            TicketHistory createdHistory = new TicketHistory();
//            createdHistory.setAction("CREATED");
//            createdHistory.setComments(comments);
//            createdHistory.setTicket(savedTicket);
//            createdHistory.setActionBy(createdByUser);
//            ticketHistoryService.save(createdHistory);
//
//            // 7. Associate history with the ticket (if not already handled by cascade/relationship mapping)
//            if (savedTicket.getHistory() == null) {
//                savedTicket.setHistory(new ArrayList<>());
//            }
//            savedTicket.getHistory().add(createdHistory);
//            // If mapping uses CascadeType.ALL, you might not need to save savedTicket again for history.
//
//
//            // 8. --- Send Email Notification via Internal API Call ---
//            String recipientEmail = createdByUser.getEmail();
//            String emailSubject = "New Ticket Created: " + newTicket.getTitle();
//            String emailBody = String.format(
//                    "<!DOCTYPE html>" +
//                    "<html lang='en'>" +
//                    "<head>" +
//                    "    <meta charset='UTF-8'>" +
//                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
//                    "    <title>New Ticket Created Notification</title>" +
//                    "    <style>" +
//                    "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
//                    "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
//                    "        .header { background-color: #007bff; padding: 20px; text-align: center; color: #ffffff; }" +
//                    "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
//                    "        .content h2 { color: #007bff; margin-top: 0; }" +
//                    "        .ticket-details { background-color: #e9ecef; padding: 20px; border-radius: 5px; margin-top: 20px; border: 1px solid #dee2e6; }" +
//                    "        .ticket-details p { margin: 5px 0; }" +
//                    "        .ticket-details strong { color: #0056b3; }" +
//                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 0.9em; border-top: 1px solid #e9ecef; }" +
//                    "        a { color: #007bff; text-decoration: none; }" +
//                    "        a:hover { text-decoration: underline; }" +
//                    "    </style>" +
//                    "</head>" +
//                    "<body>" +
//                    "    <div class='email-container'>" +
//                    "        <div class='header'>" +
//                    "            <h1>New Ticket Sent</h1>" +
//                    "        </div>" +
//                    "        <div class='content'>" +
//                    "            <p>Dear %s,</p>" +
//                    "            <p>The ticket has been sent with the following details:</p>" +
//                    "            <div class='ticket-details'>" +
//                    "                <p><strong>Ticket ID:</strong> %d</p>" +
//                    "                <p><strong>Title:</strong> %s</p>" +
//                    "                <p><strong>Description:</strong> %s</p>" +
//                    "                <p><strong>Priority:</strong> %s</p>" +
//                    "                <p><strong>Status:</strong> %s</p>" +
//                    "                <p><strong>Category:</strong> %s</p>" +
//                    "            </div>" +
//                    "            <p>Thank you for your attention to this matter.</p>" +
//                    "        </div>" +
//                    "        <div class='footer'>" +
//                    "            <p>&copy; %d Your Company Name. All rights reserved.</p>" +
//                    "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
//                    "        </div>" +
//                    "    </div>" +
//                    "</body>" +
//                    "</html>",
//                    createdByUser.getUserName(),
//                    newTicket.getTicketId(),
//                    newTicket.getTitle(),
//                    newTicket.getDescription(),
//                    newTicket.getPriority(),
//                    newTicket.getStatus(),
//                    newTicket.getCategory(),
//                    java.time.Year.now().getValue()
//            );
//
//            // Construct the URL with query parameters for the email service
//            String emailServiceUrl = UriComponentsBuilder.fromPath("/api/emails/send-async")
//                .queryParam("recipientEmail", recipientEmail)
//                .queryParam("emailSubject", emailSubject)
//                .queryParam("emailBody", emailBody)
//                .queryParam("senderEmail", senderEmail) // Example sender email
//                .toUriString();
//
//            // Make the HTTP call to your internal send-async endpoint
//            webClient.post()
//                     .uri(emailServiceUrl)
//                     .retrieve()
//                     .bodyToMono(String.class)
//                     .subscribe(
//                         response -> System.out.println("Email notification sent successfully: " + response),
//                         error -> System.err.println("Failed to send email notification: " + error.getMessage())
//                     );
//
//            // 9. Return a success response with the new ticket ID
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ApiResponse(true, "Ticket created successfully", savedTicket.getTicketId()));
//
//        } catch (IOException e) { // Catch IOException for file operations
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, "Failed to create ticket: " + e.getMessage(), null));
//        }
//    }
    
    
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
            Long ticketId = savedTicket.getTicketId();

            // 5. Handle file uploads and create Attachment entities
            List<Attachment> attachments = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                    	String fileName = FileUtils.getFileNameWithoutExtension(file) +  "_" + ticketId + "." + FileUtils.getFileExtension(file);
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
            
            
         // 8. --- Send Email Notification via JMS ---
            String recipientEmail = createdByUser.getEmail(); // Assuming User entity has an email field (changed from updatedByUser for clarity in ticket creation context)
            String emailSubject = "New Ticket Created: " + newTicket.getTitle(); // Changed from existingTicket for clarity in ticket creation context
            String emailBody = String.format(
                    "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>New Ticket Created Notification</title>" + // Updated title
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
                    "            <h1>New Ticket Created</h1>" + // Updated header title
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <p>Dear %s,</p>" +
                    "            <p>A new ticket has been created with the following details:</p>" +
                    "            <div class='ticket-details'>" +
                    "                <p><strong>Ticket ID:</strong> %d</p>" +
                    "                <p><strong>Title:</strong> %s</p>" +
                    "                <p><strong>Description:</strong> %s</p>" +
                            // Note: The original plain text included "Status", adding it here.
                    "                <p><strong>Priority:</strong> %s</p>" +
                    "                <p><strong>Status:</strong> %s</p>" +
                    "                <p><strong>Category:</strong> %s</p>" +
                    "            </div>" +
                    "            <p>Thank you for your attention to this matter.</p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>&copy; %d Your Company Name. All rights reserved.</p>" + // Current year: 2025
                    "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>",
                    createdByUser.getUserName(), // Changed from updatedByUser
                    newTicket.getTicketId(),     // Changed from existingTicket
                    newTicket.getTitle(),        // Changed from existingTicket
                    newTicket.getDescription(),  // Changed from existingTicket
                    newTicket.getPriority(),     // Changed from existingTicket
                    newTicket.getStatus(),       // Changed from existingTicket - Added Status field
                    newTicket.getCategory(),     // Changed from existingTicket
                    java.time.Year.now().getValue()
            );

            emailService.sendEmailAsync(recipientEmail, emailSubject, emailBody, senderEmail);

            // 9. Return a success response with the new ticket ID
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
                newStatus = "ASSIGNED";
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
                        	String fileName = FileUtils.getFileNameWithoutExtension(file) +  "_" + ticketId + "." + FileUtils.getFileExtension(file);
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
            
            
         // 8. --- Send Email Notification via JMS ---
            String recipientEmail = updatedByUser.getEmail(); // Assuming User entity has an email field (changed from updatedByUser for clarity in ticket creation context)
            String emailSubject = "";
            String emailBody = "";
            
            switch(newStatus) {
            	case "PENDING_APPROVAL": 
            		emailSubject = "New Ticket Created: " + existingTicket.getTitle(); // Changed from existingTicket for clarity in ticket creation context
                    emailBody = String.format(
                            "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>New Ticket Created Notification</title>" + // Updated title
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
                            "            <h1>New Ticket Created</h1>" + // Updated header title
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <p>Dear %s,</p>" +
                            "            <p>A new ticket has been created with the following details:</p>" +
                            "            <div class='ticket-details'>" +
                            "                <p><strong>Ticket ID:</strong> %d</p>" +
                            "                <p><strong>Title:</strong> %s</p>" +
                            "                <p><strong>Description:</strong> %s</p>" +
                                    // Note: The original plain text included "Status", adding it here.
                            "                <p><strong>Priority:</strong> %s</p>" +
                            "                <p><strong>Status:</strong> %s</p>" +
                            "                <p><strong>Category:</strong> %s</p>" +
                            "            </div>" +
                            "            <p>Thank you for your attention to this matter.</p>" +
                            "        </div>" +
                            "        <div class='footer'>" +
                            "            <p>&copy; %d Your Company Name. All rights reserved.</p>" + // Current year: 2025
                            "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>",
                            updatedByUser.getUserName(), // Changed from updatedByUser
                            existingTicket.getTicketId(),     // Changed from existingTicket
                            existingTicket.getTitle(),        // Changed from existingTicket
                            existingTicket.getDescription(),  // Changed from existingTicket
                            existingTicket.getPriority(),     // Changed from existingTicket
                            existingTicket.getStatus(),       // Changed from existingTicket - Added Status field
                            existingTicket.getCategory(),     // Changed from existingTicket
                            java.time.Year.now().getValue()
                    );
            		break;
            	case "ASSIGNED": 
            		emailSubject = "New Ticket Reopened: " + existingTicket.getTitle(); // Changed from existingTicket for clarity in ticket creation context
                    emailBody = String.format(
                            "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Reopened Ticket Notification</title>" + // Updated title
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
                            "            <h1>Reopened Ticket</h1>" + // Updated header title
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <p>Dear %s,</p>" +
                            "            <p>The follwoing ticket has been reopened:</p>" +
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
                            "            <p>&copy; %d Your Company Name. All rights reserved.</p>" + // Current year: 2025
                            "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>",
                            updatedByUser.getUserName(), // Changed from updatedByUser
                            existingTicket.getTicketId(),     // Changed from existingTicket
                            existingTicket.getTitle(),        // Changed from existingTicket
                            existingTicket.getDescription(),  // Changed from existingTicket
                            existingTicket.getPriority(),     // Changed from existingTicket
                            existingTicket.getStatus(),       // Changed from existingTicket - Added Status field
                            existingTicket.getCategory(),     // Changed from existingTicket
                            java.time.Year.now().getValue()
                    );
            		
            		break;
            }
            
            

            emailService.sendEmailAsync(recipientEmail, emailSubject, emailBody, senderEmail);

            return ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully", updatedTicket.getTicketId()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to update ticket: " + e.getMessage(), null));
        }
    }


//    @GetMapping("/tickets/{ticketId}/attachments")
//    public ResponseEntity<?> getAttachmentsByTicketId(@PathVariable Long ticketId) {
//        List<Attachment> attachments = attachmentService.findByTicketId(ticketId);
//        return ResponseEntity.ok(new ApiResponse(true, "Attachments retrieved successfully", attachments));
//    }
//
//    @DeleteMapping("/attachments/{attachmentId}")
//    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
//        Optional<Attachment> attachmentOptional = attachmentService.findById(attachmentId);
//        if (attachmentOptional.isPresent()) {
//            Attachment attachment = attachmentOptional.get();
//            // Optionally delete the physical file from the server as well
//            java.io.File fileToDelete = new java.io.File(attachment.getFilePath());
//            if (fileToDelete.exists()) {
//                if (fileToDelete.delete()) {
//                    attachmentService.delete(attachment);
//                    return ResponseEntity.ok(new ApiResponse(true, "Attachment deleted successfully", null));
//                } else {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body(new ApiResponse(false, "Failed to delete file from server", null));
//                }
//            } else {
//                attachmentService.delete(attachment);
//                return ResponseEntity.ok(new ApiResponse(true, "Attachment deleted successfully (file not found on server)", null));
//            }
//        } else {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Attachment not found with ID: " + attachmentId, null));
//        }
//    }
   
}