package com.ticketing.controller;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource; // Import the Resource interface

import com.ticketing.domain.Attachment;
import com.ticketing.service.AttachmentService;

//import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
	
    private final AttachmentService attachmentService;
    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);
    
    @PersistenceContext
    private EntityManager entityManager; // Inject the EntityManager

    @Value("${file.upload.directory}")
    private String uploadDirectory;
    
    public AttachmentController(AttachmentService attachmentService, EntityManager entityManager) {
        this.attachmentService = attachmentService;
        this.entityManager = entityManager;
    }
	
//	@GetMapping("/attachments/{ticketId}")
	@GetMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<?> getAttachmentsByTicketId(@PathVariable Long ticketId) {
        List<Attachment> attachments = attachmentService.findByTicketId(ticketId);
        return ResponseEntity.ok(new ApiResponse(true, "Attachments retrieved successfully", attachments));
    }

    @DeleteMapping("/{attachmentId}")
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
    

    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        //  implement the logic to retrieve the attachment.
        Optional<Attachment> attachmentOptional = attachmentService.findById(attachmentId); //findById
        if (attachmentOptional.isPresent()) {
            Attachment attachment = attachmentOptional.get();
            File file = new File(attachment.getFilePath()); //  filePath

            if (file.exists()) {
                // Load file as Resource
				Resource resource = new FileSystemResource(file);
				
             // Set the content type and other headers.
				return ResponseEntity.ok()
				        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + attachment.getFileName() + "\"")
				        .contentType(MediaType.APPLICATION_OCTET_STREAM) //generic content type
				        .contentLength(file.length())
				        .body(resource);
            } else {
                // Handle file not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            // Handle attachment not found in the database
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Changed to use the constructor
        }
    }


}
