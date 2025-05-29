package com.ticketing.service;

import com.ticketing.domain.Attachment;
import com.ticketing.domain.Ticket;
import com.ticketing.domain.TicketHistory;
import com.ticketing.domain.User;
import com.ticketing.dto.TicketDto;
import com.ticketing.dto.TicketEmailInfo;
import com.ticketing.dto.UserDto;
import com.ticketing.repository.UserRepository;
import com.ticketing.repository.TicketRepository;
//import com.ticketing.repository.TicketHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

	private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final AttachmentService attachmentService; // Inject AttachmentService
//    private final TicketHistoryRepository ticketHistoryRepository;
    private final UserService userService;
    
    private final Long SYSTEMUSER = 1652L;

    @Autowired
    public TicketService(TicketRepository ticketRepository, AttachmentService attachmentService, 
    		UserRepository userRepository,
    		UserService userService) {
        this.ticketRepository = ticketRepository;
        this.attachmentService = attachmentService;
        this.userRepository = userRepository;
        this.userService = userService;
    }
    
//    @Autowired
//    public TicketService(TicketRepository ticketRepository, AttachmentService attachmentService, 
//    		UserRepository userRepository, TicketHistoryRepository ticketHistoryRepository,
//    		UserService userService) {
//        this.ticketRepository = ticketRepository;
//        this.attachmentService = attachmentService;
//        this.userRepository = userRepository;
//        this.ticketHistoryRepository = ticketHistoryRepository;
//        this.userService = userService;
//    }
    
    @Cacheable(value = "ticketDetails", key = "#ticketId")
    @Transactional(readOnly = true) // Mark as read-only for potential optimization
    public Optional<TicketDto> getTicketDetails(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> {
                    // Fetch creator and assignee user details from User Service
                    Optional<UserDto> creator = userService.getUserById(ticket.getCreatedBy().getUserId());
                    Optional<UserDto> assignee = Optional.empty(); // Default empty
                    if (ticket.getAssignee().getUserId() != null) {
                        assignee = userService.getUserById(ticket.getAssignee().getUserId());
                    }

                    // Build and return the TicketDto
                    TicketDto dto = new TicketDto();
                    dto.setTicketId(ticket.getTicketId());
                    dto.setTitle(ticket.getTitle());
                    // ... set other fields from ticket ...

                    creator.ifPresent(u -> {
                        dto.setCreatedByUserId(u.getUserId());
                        dto.setCreatedByUserName(u.getUserName());
                    });
                    assignee.ifPresent(u -> {
                        dto.setAssigneeUserId(u.getUserId());
                        dto.setAssigneeUserName(u.getUserName());
                    });

                    return dto;
                });
    }

    @CachePut(value = "tickets", key = "#ticket.ticketId") // Update specific ticket in cache
    @CacheEvict(value = {"allTickets", "ticketsByCreatedBy", "ticketsByAssignee", "ticketsByStatus", "ticketsByPriority", "ticketsByManager", "ticketDetails"}, allEntries = true) // Evict relevant list caches
    public Ticket save(Ticket ticket) {
    	System.out.println("Saving/Updating Ticket in DB and refreshing caches: " + ticket.getTicketId());
        return ticketRepository.save(ticket);
    }

    @Cacheable("tickets") // Cache individual tickets by their ID
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(Long ticketId) {
    	System.out.println("Fetching Ticket by ID from DB or Cache: " + ticketId);
        return ticketRepository.findById(ticketId);
    }

    @Cacheable("allTickets") // Cache the entire list of tickets
    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
    	System.out.println("Fetching all Tickets from DB or Cache...");
        return ticketRepository.findAll();
    }

    @CacheEvict(value = {"tickets", "allTickets", "ticketsByCreatedBy", "ticketsByAssignee", "ticketsByStatus", "ticketsByPriority", "ticketsByManager", "ticketDetails",
            "ticketsResolvedAndRejectedUnupdated", "ticketsPendingApprovalUnupdated", "ticketsAssignedUnupdated"}, allEntries = true) // Evict from multiple caches
    public void deleteById(Long ticketId) {
    	System.out.println("Deleting Ticket from DB and evicting caches: " + ticketId);
        ticketRepository.deleteById(ticketId);
    }
    
    @Cacheable(value = "ticketCreator", key = "#ticketId") // Cache the creator user by ticket ID
    @Transactional(readOnly = true)
    public User findCreator(Long ticketId) {
    	System.out.println("Fetching creator for Ticket ID " + ticketId + " from DB or Cache.");
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId)); 
        return ticket.getCreatedBy();
    }
    

    @Cacheable(value = "ticketsByCreatedBy", key = "#createdByUserId")
    @Transactional(readOnly = true)
    public List<Ticket> findByCreatedBy(Long createdByUserId) {
    	System.out.println("Fetching Tickets by CreatedBy User ID " + createdByUserId + " from DB or Cache.");
        return ticketRepository.findByCreatedBy_UserId(createdByUserId);
    }
    
    @Cacheable(value = "ticketsByManager", key = "#manager.userId") // Use manager's userId as cache key
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByManager(User manager) {
    	System.out.println("Fetching Tickets by Manager " + manager.getUserId() + " from DB or Cache.");
        // Find tickets created by users managed by the given manager.
        return ticketRepository.findByCreatedBy_Manager_UserId(manager.getUserId());
    }

    @Cacheable(value = "ticketsByAssignee", key = "#assigneeUserId")
    @Transactional(readOnly = true)
    public List<Ticket> findByAssignee(Long assigneeUserId) {
    	System.out.println("Fetching Tickets by Assignee User ID " + assigneeUserId + " from DB or Cache.");
        return ticketRepository.findByAssignee_UserId(assigneeUserId);
    }

    @Cacheable(value = "ticketsByStatus", key = "#status")
    @Transactional(readOnly = true)
    public List<Ticket> findByStatus(String status) {
    	System.out.println("Fetching Tickets by Status " + status + " from DB or Cache.");
        return ticketRepository.findByStatus(status);
    }

    @Cacheable(value = "ticketsByPriority", key = "#priority")
    @Transactional(readOnly = true)
    public List<Ticket> findByPriority(String priority) {
    	System.out.println("Fetching Tickets by Priority " + priority + " from DB or Cache.");
        return ticketRepository.findByPriority(priority);
    }

    @CachePut(value = "tickets", key = "#id") // Update specific ticket in cache
    @CacheEvict(value = {
            "allTickets",              // Invalidate general list
            "ticketsByCreatedBy",      // Could change if creator logic allows update (less common)
            "ticketsByAssignee",       // Assignee might change
            "ticketsByStatus",         // Status will likely change
            "ticketsByPriority",       // Priority might change
            "ticketsByManager",        // Could change if creator logic allows update
            "ticketDetails",           // DTO details change
            "ticketsResolvedAndRejectedUnupdated", // Status changes could affect these
            "ticketsPendingApprovalUnupdated",     // Status changes could affect these
            "ticketsAssignedUnupdated"             // Status changes could affect these
        }, allEntries = true) // Evict all entries from these relevant list caches
    @Transactional
    public Ticket updateTicket(Long id, Ticket updatedTicket, List<Attachment> newAttachments) {
    	System.out.println("Updating Ticket (with attachments) in DB and refreshing caches: " + id);
        return ticketRepository.findById(id)
                .map(existingTicket -> {
                    if (updatedTicket.getTitle() != null) {
                        existingTicket.setTitle(updatedTicket.getTitle());
                    }
                    if (updatedTicket.getDescription() != null) {
                        existingTicket.setDescription(updatedTicket.getDescription());
                    }
                    if (updatedTicket.getPriority() != null) {
                        existingTicket.setPriority(updatedTicket.getPriority());
                    }
                    if (updatedTicket.getStatus() != null) {
                        existingTicket.setStatus(updatedTicket.getStatus());
                    }
                    if (updatedTicket.getCategory() != null) {
                        existingTicket.setCategory(updatedTicket.getCategory());
                    }
                    // We are now handling attachments separately
                    // if (updatedTicket.getFileAttachmentPath() != null) {
                    //     existingTicket.setFileAttachmentPath(updatedTicket.getFileAttachmentPath());
                    // }
                    if (updatedTicket.getAssignee() != null) {
                        existingTicket.setAssignee(updatedTicket.getAssignee());
                    }

                    Ticket savedTicket = ticketRepository.save(existingTicket);

                    // Handle new attachments
                    if (newAttachments != null && !newAttachments.isEmpty()) {
                        newAttachments.forEach(attachment -> {
                            attachment.setTicket(savedTicket);
                            attachmentService.save(attachment);
                        });
                        // After saving new attachments, you might want to reload the ticket
                        // to have the updated list of attachments if needed immediately.
                        // Optional: return ticketRepository.findById(savedTicket.getTicketId()).orElse(savedTicket);
                    }

                    return savedTicket;
                })
                .orElse(null);
    }

    @CachePut(value = "tickets", key = "#id") // Update specific ticket in cache
    @CacheEvict(value = {
            "allTickets",
            "ticketsByCreatedBy",
            "ticketsByAssignee",
            "ticketsByStatus",
            "ticketsByPriority",
            "ticketsByManager",
            "ticketDetails",
            "ticketsResolvedAndRejectedUnupdated",
            "ticketsPendingApprovalUnupdated",
            "ticketsAssignedUnupdated"
        }, allEntries = true) // Evict all entries from these relevant list caches
    @Transactional
    public Ticket updateTicketWithoutNewAttachments(Long id, Ticket updatedTicket) {
    	System.out.println("Updating Ticket (without new attachments) in DB and refreshing caches: " + id);
        return ticketRepository.findById(id)
                .map(existingTicket -> {
                    if (updatedTicket.getTitle() != null) {
                        existingTicket.setTitle(updatedTicket.getTitle());
                    }
                    if (updatedTicket.getDescription() != null) {
                        existingTicket.setDescription(updatedTicket.getDescription());
                    }
                    if (updatedTicket.getPriority() != null) {
                        existingTicket.setPriority(updatedTicket.getPriority());
                    }
                    if (updatedTicket.getStatus() != null) {
                        existingTicket.setStatus(updatedTicket.getStatus());
                    }
                    if (updatedTicket.getCategory() != null) {
                        existingTicket.setCategory(updatedTicket.getCategory());
                    }
                    // Do not handle attachments here
                    if (updatedTicket.getAssignee() != null) {
                        existingTicket.setAssignee(updatedTicket.getAssignee());
                    }
                    return ticketRepository.save(existingTicket);
                })
                .orElse(null);
    }
    
    
    
    @Cacheable(value = "ticketsPendingApprovalUnupdated", key = "#days")
    @Transactional(readOnly = true)
    public List<TicketEmailInfo> getTicketsPendingApprovalUnupdatedForDays(int days) {
    	System.out.println("Fetching PENDING_APPROVAL tickets unupdated for " + days + " days from DB or Cache.");
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative.");
        }

        // Calculate the threshold date: 'days' days ago from the start of today.
        // We convert LocalDate to java.util.Date for JPA query comparison.
        // The `atStartOfDay` ensures we compare against the beginning of that day.
        LocalDate thresholdLocalDate = LocalDate.now().minusDays(days);
        Date thresholdDate = Date.from(thresholdLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Use the custom repository query to fetch relevant Ticket entities
        List<Ticket> unupdatedTickets = ticketRepository.findTicketsNotUpdatedSinceConsideringCreation(thresholdDate);

        // Map Ticket entities to TicketEmailInfo DTOs
        return unupdatedTickets.stream().filter(ticket -> ticket.getStatus().equals("PENDING_APPROVAL"))
                .map(ticket -> {
      
                    Date latestUpdateDate = ticket.getCreationDate();
                    if (ticket.getHistory() != null && !ticket.getHistory().isEmpty()) {
                        latestUpdateDate = ticket.getHistory().stream()
                                .map(TicketHistory::getActionDate)
                                .max(Date::compareTo) // Find the maximum (latest) date
                                .orElse(ticket.getCreationDate()); // Fallback if history is somehow empty
                    }

                    User user = (ticket != null) ? ticket.getCreatedBy() : null;
                    String managerName = (user != null) ? user.getManager().getUserName() : null;
        
                 // --- FORMATTING THE EMAIL BODY ---
                    String body = String.format(
                            "<!DOCTYPE html>" +
                                    "<html lang='en'>" +
                                    "<head>" +
                                    "    <meta charset='UTF-8'>" +
                                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                    "    <title>Ticket Inactivity Notification</title>" + // Updated title for this context
                                    "    <style>" +
                                    "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                                    "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                                    "        .header { background-color: #FFD700; padding: 20px; text-align: center; color: #000000; }" + // Changed header background to yellow and text to black
                                    "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
                                    "        .content h2 { color: #dc3545; margin-top: 0; }" + // Changed h2 color
                                    "        .ticket-details { background-color: #FFD700; padding: 20px; border-radius: 5px; margin-top: 20px; border: 1px solid #f8d7da; }" + // Changed details background/border
                                    "        .ticket-details p { margin: 5px 0; }" +
                                    "        .ticket-details strong { color: #000000; }" + // Changed strong color
                                    "        .footer { background-color: #FFD700; padding: 20px; text-align: center; color: #6c757d; font-size: 0.9em; border-top: 1px solid #e9ecef; }" +
                                    "        a { color: #007bff; text-decoration: none; }" +
                                    "        a:hover { text-decoration: underline; }" +
                                    "    </style>" +
                                    "</head>" +
                                    "<body>" +
                                    "    <div class='email-container'>" +
                                    "        <div class='header'>" +
                                    "            <h1>PENDING_APPROVAL Ticket have not been updated %d</h1>" + // Updated header title for this context
                                    "        </div>" +
                                    "        <div class='content'>" +
                                    "            <p>Dear %s,</p>" + // User name placeholder
                                    "            <p>This is an automated notification. Ticket <strong>#%d</strong> need to be reviewed, it has not been updated for %d days.</p>" + // Added days
                                    "            <p>Here are the details of the PENDING_APPROVAL ticket:</p>" +
                                    "            <div class='ticket-details'>" +
                                    "                <p><strong>Ticket ID:</strong> %d</p>" +
                                    "                <p><strong>Title:</strong> %s</p>" +
                                    "                <p><strong>Description:</strong> %s</p>" +
                                    "                <p><strong>Priority:</strong> %s</p>" +
                                    "                <p><strong>Status:</strong> %s</p>" +
                                    "                <p><strong>Category:</strong> %s</p>" +
                                    "            </div>" +
                                    "            <p>Thank you.</p>" +
                                    "        </div>" +
                                    "        <div class='footer'>" +
                                    "            <p>&copy; %d Your Company Name. All rights reserved.</p>" +
                                    "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                                    "        </div>" +
                                    "    </div>" +
                                    "</body>" +
                                    "</html>",
                            days,
                            managerName, // %s for Dear %s,
                            ticket.getTicketId(), // %d for ticket #%d
                            days, // %d for for %d days
                            ticket.getTicketId(),
                            ticket.getTitle(),
                            ticket.getDescription(),
                            ticket.getPriority(),
                            ticket.getStatus(), // Assuming status is already "CLOSED" or will be updated
                            ticket.getCategory(),
                            java.time.Year.now().getValue() // Current year for copyright
                    );
                    
                    Long ticketId = ticket.getTicketId();
                    String managerEmail = (user != null) ? user.getManager().getEmail() : null;
                    String subject = "The ticket " + ticket.getTicketId() + " is still in PENDING_APPROVAL status for more than " + days + " days";

                    return new TicketEmailInfo(
                    		ticketId,
                    		managerEmail,
                    		subject,
                    		body
                    );
                })
                .collect(Collectors.toList());
    }
    
    
    @Cacheable(value = "ticketsAssignedUnupdated", key = "#days")
    @Transactional(readOnly = true)
    public List<TicketEmailInfo> getTicketsAssigedUnupdatedForDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative.");
        }

        // Calculate the threshold date: 'days' days ago from the start of today.
        // We convert LocalDate to java.util.Date for JPA query comparison.
        // The `atStartOfDay` ensures we compare against the beginning of that day.
        LocalDate thresholdLocalDate = LocalDate.now().minusDays(days);
        Date thresholdDate = Date.from(thresholdLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Use the custom repository query to fetch relevant Ticket entities
        List<Ticket> unupdatedTickets = ticketRepository.findTicketsNotUpdatedSinceConsideringCreation(thresholdDate);

        // Map Ticket entities to TicketEmailInfo DTOs
        return unupdatedTickets.stream().filter(ticket -> ticket.getStatus().equals("ASSIGNED"))
                .map(ticket -> {
      
                    Date latestUpdateDate = ticket.getCreationDate();
                    if (ticket.getHistory() != null && !ticket.getHistory().isEmpty()) {
                        latestUpdateDate = ticket.getHistory().stream()
                                .map(TicketHistory::getActionDate)
                                .max(Date::compareTo) // Find the maximum (latest) date
                                .orElse(ticket.getCreationDate()); // Fallback if history is somehow empty
                    }

                    String assigneeName = (ticket != null) ? ticket.getAssignee().getUserName() : null;
        
                 // --- FORMATTING THE EMAIL BODY ---
                    String body = String.format(
                            "<!DOCTYPE html>" +
                                    "<html lang='en'>" +
                                    "<head>" +
                                    "    <meta charset='UTF-8'>" +
                                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                    "    <title>Ticket Inactivity Notification</title>" + // Updated title for this context
                                    "    <style>" +
                                    "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                                    "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                                    "        .header { background-color: #FFD700; padding: 20px; text-align: center; color: #000000; }" + // Changed header background to yellow and text to black
                                    "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
                                    "        .content h2 { color: #dc3545; margin-top: 0; }" + // Changed h2 color
                                    "        .ticket-details { background-color: #FFD700; padding: 20px; border-radius: 5px; margin-top: 20px; border: 1px solid #f8d7da; }" + // Changed details background/border
                                    "        .ticket-details p { margin: 5px 0; }" +
                                    "        .ticket-details strong { color: #c82333; }" + // Changed strong color
                                    "        .footer { background-color: #FFD700; padding: 20px; text-align: center; color: #6c757d; font-size: 0.9em; border-top: 1px solid #e9ecef; }" +
                                    "        a { color: #007bff; text-decoration: none; }" +
                                    "        a:hover { text-decoration: underline; }" +
                                    "    </style>" +
                                    "</head>" +
                                    "<body>" +
                                    "    <div class='email-container'>" +
                                    "        <div class='header'>" +
                                    "            <h1>ASSIGNED Ticket have not been updated %d</h1>" + // Updated header title for this context
                                    "        </div>" +
                                    "        <div class='content'>" +
                                    "            <p>Dear %s,</p>" + // User name placeholder
                                    "            <p>This is an automated notification. Ticket <strong>#%d</strong> need to be reviewed, it has not been updated for %d days.</p>" + // Added days
                                    "            <p>Here are the details of the ASSIGNED ticket:</p>" +
                                    "            <div class='ticket-details'>" +
                                    "                <p><strong>Ticket ID:</strong> %d</p>" +
                                    "                <p><strong>Title:</strong> %s</p>" +
                                    "                <p><strong>Description:</strong> %s</p>" +
                                    "                <p><strong>Priority:</strong> %s</p>" +
                                    "                <p><strong>Status:</strong> %s</p>" +
                                    "                <p><strong>Category:</strong> %s</p>" +
                                    "            </div>" +
                                    "            <p>Thank you.</p>" +
                                    "        </div>" +
                                    "        <div class='footer'>" +
                                    "            <p>&copy; %d Your Company Name. All rights reserved.</p>" +
                                    "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                                    "        </div>" +
                                    "    </div>" +
                                    "</body>" +
                                    "</html>",
                            days,
                            assigneeName, // %s for Dear %s,
                            ticket.getTicketId(), // %d for ticket #%d
                            days, // %d for for %d days
                            ticket.getTicketId(),
                            ticket.getTitle(),
                            ticket.getDescription(),
                            ticket.getPriority(),
                            ticket.getStatus(), // Assuming status is already "CLOSED" or will be updated
                            ticket.getCategory(),
                            java.time.Year.now().getValue() // Current year for copyright
                    );
                    
                    Long ticketId = ticket.getTicketId();
                    String assigneddEmail = (ticket != null) ? ticket.getAssignee().getEmail() : null;
                    
                    String subject = "The ticket " + ticket.getTicketId() + " is still in PENDING_APPROVAL status for more than " + days + " days";

                    return new TicketEmailInfo(
                    		ticketId,
                    		assigneddEmail,
                    		subject,
                    		body
                    );
                })
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "ticketsResolvedAndRejectedUnupdated", key = "#days")
    @Transactional(readOnly = true)
    public List<TicketEmailInfo> getTicketsResolvedAdnRejectedUnupdatedForDays(int days) {
    	System.out.println("Fetching RESOLVED/REJECTED tickets unupdated for " + days + " days from DB or Cache.");
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative.");
        }

        // Calculate the threshold date: 'days' days ago from the start of today.
        // We convert LocalDate to java.util.Date for JPA query comparison.
        // The `atStartOfDay` ensures we compare against the beginning of that day.
        LocalDate thresholdLocalDate = LocalDate.now().minusDays(days);
        Date thresholdDate = Date.from(thresholdLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Use the custom repository query to fetch relevant Ticket entities
        List<Ticket> unupdatedTickets = ticketRepository.findTicketsNotUpdatedSinceConsideringCreation(thresholdDate);

        // Map Ticket entities to TicketEmailInfo DTOs
        return unupdatedTickets.stream().filter(ticket -> ticket.getStatus().equals("RESOLVED") || ticket.getStatus().equals("REJECTED"))
                .map(ticket -> {
      
                    Date latestUpdateDate = ticket.getCreationDate();
                    if (ticket.getHistory() != null && !ticket.getHistory().isEmpty()) {
                        latestUpdateDate = ticket.getHistory().stream()
                                .map(TicketHistory::getActionDate)
                                .max(Date::compareTo) // Find the maximum (latest) date
                                .orElse(ticket.getCreationDate()); // Fallback if history is somehow empty
                    }

                    // Extract reporter emails safely
                    
                    String reporterName = (ticket.getCreatedBy() != null) ? ticket.getCreatedBy().getUserName() : null;
                    
                    // --- FORMATTING THE EMAIL BODY ---
                    String body = String.format(
                            "<!DOCTYPE html>" +
                                    "<html lang='en'>" +
                                    "<head>" +
                                    "    <meta charset='UTF-8'>" +
                                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                    "    <title>Ticket Inactivity Notification</title>" + // Updated title for this context
                                    "    <style>" +
                                    "        body { font-family: 'Inter', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                                    "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                                    "        .header { background-color: #dc3545; padding: 20px; text-align: center; color: #ffffff; }" + // Changed header color for "closed" context
                                    "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
                                    "        .content h2 { color: #dc3545; margin-top: 0; }" + // Changed h2 color
                                    "        .ticket-details { background-color: #ffe0e6; padding: 20px; border-radius: 5px; margin-top: 20px; border: 1px solid #f8d7da; }" + // Changed details background/border
                                    "        .ticket-details p { margin: 5px 0; }" +
                                    "        .ticket-details strong { color: #c82333; }" + // Changed strong color
                                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 0.9em; border-top: 1px solid #e9ecef; }" +
                                    "        a { color: #007bff; text-decoration: none; }" +
                                    "        a:hover { text-decoration: underline; }" +
                                    "    </style>" +
                                    "</head>" +
                                    "<body>" +
                                    "    <div class='email-container'>" +
                                    "        <div class='header'>" +
                                    "            <h1>Ticket Closed Due to Inactivity</h1>" + // Updated header title for this context
                                    "        </div>" +
                                    "        <div class='content'>" +
                                    "            <p>Dear %s,</p>" + // User name placeholder
                                    "            <p>This is an automated notification. Ticket <strong>#%d</strong> has been automatically closed because it has not been updated for %d days.</p>" + // Added days
                                    "            <p>Here are the details of the closed ticket:</p>" +
                                    "            <div class='ticket-details'>" +
                                    "                <p><strong>Ticket ID:</strong> %d</p>" +
                                    "                <p><strong>Title:</strong> %s</p>" +
                                    "                <p><strong>Description:</strong> %s</p>" +
                                    "                <p><strong>Priority:</strong> %s</p>" +
                                    "                <p><strong>Status:</strong> %s</p>" +
                                    "                <p><strong>Category:</strong> %s</p>" +
                                    "            </div>" +
                                    "            <p>If you believe this ticket was closed in error or require further assistance, please contact support or create a new ticket.</p>" +
                                    "            <p>Thank you.</p>" +
                                    "        </div>" +
                                    "        <div class='footer'>" +
                                    "            <p>&copy; %d Your Company Name. All rights reserved.</p>" +
                                    "            <p><a href='#'>Unsubscribe</a> | <a href='#'>Privacy Policy</a></p>" +
                                    "        </div>" +
                                    "    </div>" +
                                    "</body>" +
                                    "</html>",
                            reporterName, // %s for Dear %s,
                            ticket.getTicketId(), // %d for ticket #%d
                            days, // %d for for %d days
                            ticket.getTicketId(),
                            ticket.getTitle(),
                            ticket.getDescription(),
                            ticket.getPriority(),
                            ticket.getStatus(), // Assuming status is already "CLOSED" or will be updated
                            ticket.getCategory(),
                            java.time.Year.now().getValue() // Current year for copyright
                    );
                    
                    Long ticketId = ticket.getTicketId();
                    String reporterEmail = (ticket.getCreatedBy() != null) ? ticket.getCreatedBy().getEmail() : null;
                    String subject = "The ticket " + ticket.getTicketId() + " have been closed";
                    
                    return new TicketEmailInfo(
                    		ticketId,
                    		reporterEmail,
                    		subject,
                    		body
                    );
                })
                .collect(Collectors.toList());
    }
    
    @CachePut(value = "tickets", key = "#ticketId") // Update the specific ticket in cache
    @CacheEvict(value = {
            "allTickets",              // General list becomes stale
            "ticketsByCreatedBy",      // If 'createdBy' lists are filtered by status
            "ticketsByAssignee",       // If 'assignee' lists are filtered by status
            "ticketsByStatus",         // *Crucial*: Status changes from ASSIGNED/PENDING_APPROVAL/OPEN to CLOSED
            "ticketsByPriority",       // If 'priority' lists are filtered by status
            "ticketsByManager",        // If 'manager' lists are filtered by status
            "ticketDetails",           // TicketDto will be stale
            "ticketsPendingApprovalUnupdated", // Ticket no longer pending approval
            "ticketsAssignedUnupdated",        // Ticket no longer assigned and unupdated
            // Note: "ticketsResolvedAndRejectedUnupdated" might still contain it,
            // but its status is now CLOSED, so it might need a separate consideration
            // if that report explicitly looks for RESOLVED/REJECTED only.
            // For safety, including it here if its filter was broader (e.g., non-OPEN)
            "ticketsResolvedAndRejectedUnupdated"
        }, allEntries = true) // Evict all entries from these relevant list caches
    @Transactional // Ensures the entire method executes in a single transaction
    public Ticket closeById(Long ticketId, int days) {
    	System.out.println("Closing Ticket " + ticketId + " and refreshing caches.");
    	
        // 1. Find the ticket by ID
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        String status = ticket.getStatus();
        Date today = new Date();
        String comments = "Closed by System beacuse inactivity for mote than " + days + " days";
        
        User systemUser = userRepository.findById(SYSTEMUSER).orElseThrow(() -> new RuntimeException("System user not found"));
        
        if (!status.equals("CLOSED")) {
        	// 2. Update the ticket status and closed timestamp
        	ticket.setStatus("CLOSED");
        	
        	// 3.Update Ticket History
        	List<TicketHistory> historyList = ticket.getHistory();
        	
        	TicketHistory historyClosed = new TicketHistory();

        	historyClosed.setAction("CLOSED");
        	historyClosed.setComments(comments);
        	historyClosed.setTicket(ticket);
        	historyClosed.setActionBy(systemUser);
//        	historyClosed.setActionDate(today);
        	
        	historyList.add(historyClosed);
        	
        	ticket.setHistory(historyList);

        }

        // 3. Save the updated ticket back to the database
        // Spring Data JPA's save method handles both new entities and updates
        return ticketRepository.save(ticket);
    }
    
    
    
    

}