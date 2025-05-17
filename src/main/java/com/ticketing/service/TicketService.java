package com.ticketing.service;

import com.ticketing.domain.Attachment;
import com.ticketing.domain.Ticket;
import com.ticketing.domain.User;
import com.ticketing.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AttachmentService attachmentService; // Inject AttachmentService

    @Autowired
    public TicketService(TicketRepository ticketRepository, AttachmentService attachmentService) {
        this.ticketRepository = ticketRepository;
        this.attachmentService = attachmentService;
    }

    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }
    
    public User findCreator(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId)); 
        return ticket.getCreatedBy();
    }

    public List<Ticket> findByCreatedBy(Long createdByUserId) {
        return ticketRepository.findByCreatedBy_UserId(createdByUserId);
    }

    public List<Ticket> findByAssignee(Long assigneeUserId) {
        return ticketRepository.findByAssignee_UserId(assigneeUserId);
    }

    public List<Ticket> findByStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> findByPriority(String priority) {
        return ticketRepository.findByPriority(priority);
    }

    @Transactional
    public Ticket updateTicket(Long id, Ticket updatedTicket, List<Attachment> newAttachments) {
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

    @Transactional
    public Ticket updateTicketWithoutNewAttachments(Long id, Ticket updatedTicket) {
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
}