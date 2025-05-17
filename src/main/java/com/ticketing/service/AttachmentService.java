package com.ticketing.service;

import org.springframework.stereotype.Service;

import com.ticketing.domain.Attachment;
import com.ticketing.repository.AttachmentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment save(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public Optional<Attachment> findById(Long id) {
        return attachmentRepository.findById(id);
    }

    public List<Attachment> findByTicketId(Long ticketId) {
        return attachmentRepository.findByTicket_TicketId(ticketId);
    }

    public void delete(Attachment attachment) {
        attachmentRepository.delete(attachment);
    }

    public void deleteById(Long id) {
        attachmentRepository.deleteById(id);
    }
}
