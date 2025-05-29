package com.ticketing.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    @CachePut(value = "attachments", key = "#attachment.id")
    public Attachment save(Attachment attachment) {
        System.out.println("Saving/Updating Attachment in DB and Cache: " + attachment.getAttachmentId());
        return attachmentRepository.save(attachment);
    }

    @Cacheable("attachments")
    public Optional<Attachment> findById(Long id) {
        System.out.println("Fetching Attachment from DB or Cache: " + id);
        return attachmentRepository.findById(id);
    }

    // For methods that return a list, caching can be trickier if you want to cache individual items.
    // Caching the entire list for a given ticketId might be an option, but be mindful of its size.
    // For simplicity, we won't cache this method at the individual attachment level here,
    // as it's not a direct lookup by a single entity ID.
    public List<Attachment> findByTicketId(Long ticketId) {
        return attachmentRepository.findByTicket_TicketId(ticketId);
    }

    @CacheEvict(value = "attachments", key = "#attachment.id")
    public void delete(Attachment attachment) {
        System.out.println("Deleting Attachment from DB and Cache: " + attachment.getAttachmentId());
        attachmentRepository.delete(attachment);
    }

    @CacheEvict(value = "attachments", key = "#id")
    public void deleteById(Long id) {
        System.out.println("Deleting Attachment from DB and Cache: " + id);
        attachmentRepository.deleteById(id);
    }
}