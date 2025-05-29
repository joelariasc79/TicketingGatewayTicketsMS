// src/main/java/com/ticketing/service/PdfGeneratorService.java
package com.ticketing.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Value;

import com.ticketing.domain.User;
import com.ticketing.domain.Ticket;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files; 
import java.nio.file.Path; 
import java.nio.file.Paths; 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    @Value("${app.pdf.storage-base-dir}")
    private String pdfStorageBaseDir;

    @Value("${file.logo}")
    private String logoPathProperty;

    /**
     * Generates a PDF for a resolved ticket and saves it to the file system.
     *
     * @param ticket The ticket entity with resolution details.
     * @param comments The comments provided for the resolution.
     * @return The absolute path to the generated PDF file.
     * @throws DocumentException If there's an iText specific error during PDF generation.
     * @throws IOException If there's an I/O error during file operations or image loading.
     */
    public String generateResolutionPdf(Ticket ticket, String comments) throws DocumentException, IOException {
        String pdfFileName = "ticket_resolution_" + ticket.getTicketId() + "_" + System.currentTimeMillis() + ".pdf";
        File storageDir = new File(pdfStorageBaseDir);
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // Create directories if they don't exist
        }
        String pdfFilePath = storageDir.getAbsolutePath() + File.separator + pdfFileName;

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFilePath));
        document.open();

        // Add Logo
        try {
        	System.out.println("logoPathProperty: " + logoPathProperty);
            Image logo = Image.getInstance(new ClassPathResource(logoPathProperty).getURL());
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);
        } catch (IOException | BadElementException e) {
            System.err.println("Could not load logo: " + e.getMessage());
            // Proceed without logo if not found
        }

        // Add Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Ticket Resolution Details", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add Ticket Details
        Font detailFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

        User requester = ticket.getCreatedBy();
        String requesterInfo = (requester != null) ?
                               requester.getUserName() + " (" + requester.getEmail() + ")" : "N/A";
        String assigneeInfo = (ticket.getAssignee() != null) ?
                              ticket.getAssignee().getUserName() : "N/A";

     // Corrected way to add Paragraphs with Chunks
        Paragraph pTicketId = new Paragraph("Ticket ID: ", boldFont);
        pTicketId.add(new Chunk(String.valueOf(ticket.getTicketId()), detailFont));
        document.add(pTicketId); // This was line 80

        Paragraph pSubject = new Paragraph("Title: ", boldFont);
        pSubject.add(new Chunk(ticket.getTitle(), detailFont));
        document.add(pSubject);

        Paragraph pStatus = new Paragraph("Status: ", boldFont);
        pStatus.add(new Chunk(ticket.getStatus(), detailFont));
        document.add(pStatus);

        Paragraph pRequestedBy = new Paragraph("Requested By: ", boldFont);
        pRequestedBy.add(new Chunk(requesterInfo, detailFont));
        document.add(pRequestedBy);

        Paragraph pAssignedTo = new Paragraph("Assigned To: ", boldFont);
        pAssignedTo.add(new Chunk(assigneeInfo, detailFont));
        document.add(pAssignedTo);

        Paragraph pResolutionDate = new Paragraph("Resolution Date: ", boldFont);
        pResolutionDate.add(new Chunk(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), detailFont));
        document.add(pResolutionDate);

        document.add(Chunk.NEWLINE); // This adds a new line for spacing.

        // Add Resolution Comments
        Font commentsTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        document.add(new Paragraph("Resolution Comments:", commentsTitleFont));
        document.add(new Paragraph(comments, detailFont));

        document.close();

        return pdfFilePath;
    }
    
    public boolean deletePdfFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Attempted to delete file with null or empty path.");
            return false;
        }

        Path path = Paths.get(filePath);
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted successfully: " + filePath);
                return true;
            } else {
                System.out.println("File not found, skipping deletion: " + filePath);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error deleting file " + filePath + ": " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return false;
        }
    }
}