package com.ticketing.service;

import com.ticketing.dto.messages.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JmsTemplate jmsTemplate;
    private final JavaMailSender mailSender;

    @Value("${app.jms.ticket-email-queue}")
    private String ticketEmailQueue;
    
    @Value("${app.jms.ticket-email-attachment-queue}")
    private String ticketEmailAttachmentQueue;
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    @Autowired
    public EmailService(JmsTemplate jmsTemplate, JavaMailSender mailSender) {
        this.jmsTemplate = jmsTemplate;
        this.mailSender = mailSender;
    }
    
    // ************************************************************
    // Producers:
    // ************************************************************
    
    public void sendEmailAsync(String to, String subject, String body, String sender) {
        EmailMessage emailMessage = new EmailMessage(to, subject, body, sender);
        try {
            jmsTemplate.convertAndSend(ticketEmailQueue, emailMessage);
            System.out.println("Email message sent to JMS queue: " + ticketEmailQueue + " for recipient: " + to);
        } catch (Exception e) {
            System.err.println("Error sending email message to JMS queue: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    // This method is for PRODUCING an async email with single PDF.
    // It puts the EmailMessage with PDF bytes onto the JMS queue.
    public void sendEmailAsync(String to, String subject, String body, String sender, String pdfFilePath) {
        try {
            File pdfFile = new File(pdfFilePath);
            if (!pdfFile.exists() || !pdfFile.isFile()) {
                throw new IllegalArgumentException("PDF file not found or is not a valid file: " + pdfFilePath);
            }

            byte[] pdfBytes = Files.readAllBytes(Paths.get(pdfFilePath));
            String fileName = pdfFile.getName();
            String contentType = "application/pdf";

            EmailMessage.Attachment pdfAttachment = new EmailMessage.Attachment(fileName, contentType, pdfBytes);
            List<EmailMessage.Attachment> attachments = Arrays.asList(pdfAttachment);

            EmailMessage emailMessage = new EmailMessage(to, subject, body, sender, attachments);

            jmsTemplate.convertAndSend(ticketEmailAttachmentQueue, emailMessage);
            System.out.println("Email message with PDF attachment sent to JMS queue: " + ticketEmailAttachmentQueue + " for recipient: " + to + " (File: " + fileName + ")");
        } catch (Exception e) {
            System.err.println("Error sending email message with attachment to JMS queue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // This method is for PRODUCING an async email with multiple attachments.
    // It puts the EmailMessage with multiple attachment bytes onto the JMS queue.
    public void sendEmailAsync(String to, String subject, String body, String sender, List<String> filePaths) {
        try {
            List<EmailMessage.Attachment> attachments = new ArrayList<>();
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    System.err.println("Skipping invalid file: " + filePath);
                    continue;
                }
                byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                String fileName = file.getName();
                String contentType = Files.probeContentType(Paths.get(filePath));
                if (contentType == null) {
                    if (fileName.toLowerCase().endsWith(".pdf")) {
                        contentType = "application/pdf";
                    } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (fileName.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    } else {
                        contentType = "application/octet-stream";
                    }
                }
                attachments.add(new EmailMessage.Attachment(fileName, contentType, fileBytes));
            }

            EmailMessage emailMessage = new EmailMessage(to, subject, body, sender, attachments);

            jmsTemplate.convertAndSend(ticketEmailAttachmentQueue, emailMessage);
            System.out.println("Email message with multiple attachments sent to JMS queue: " + ticketEmailAttachmentQueue + " for recipient: " + to);
        } catch (Exception e) {
            System.err.println("Error sending email message with multiple attachments to JMS queue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    // ************************************************************
    // Consumers:
    // ************************************************************
    

    public void sendHtmlEmail(String to, String subject, String htmlBody, String from) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // 'true' for multipart, 'UTF-8' encoding

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // <-- CRUCIAL: 'true' indicates HTML content

            mailSender.send(mimeMessage);
            System.out.println("HTML Email successfully sent to " + to + " from " + from);
        } catch (MailException e) {
            System.err.println("Error sending HTML email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Error creating MimeMessage for " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to send email with a list of attachments (actual sending, not async)
    public void sendEmailWithAttachments(String to, String subject, String body, String from, List<EmailMessage.Attachment> attachments) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // 'true' for multipart email (required for attachments)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // Assuming the body can be HTML too

            if (attachments != null && !attachments.isEmpty()) {
                for (EmailMessage.Attachment attachment : attachments) {
                    // Add attachment from byte array
                    helper.addAttachment(attachment.getFileName(), new jakarta.mail.util.ByteArrayDataSource(attachment.getFileContent(), attachment.getContentType()));
                }
            }

            mailSender.send(mimeMessage);
            System.out.println("Email with attachments successfully sent to " + to + " from " + from);
        } catch (MailException e) {
            System.err.println("Error sending email with attachments to " + to + ": " + e.getMessage());
            e.printStackTrace();
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Error creating MimeMessage with attachments for " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}