package com.ticketing.util;

import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    public static String getFileNameWithoutExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return ""; // Or throw an exception, depending on your needs
        }
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return originalFileName; // No extension found
        }
        return originalFileName.substring(0, lastDotIndex);
    }

    public static String getFileExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == originalFileName.length() - 1) {
            return ""; // No extension or filename ends with a dot
        }
        return originalFileName.substring(lastDotIndex + 1);
    }

    // Example usage in a controller or service:
    // @PostMapping("/upload")
    // public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    //     String fileName = getFileNameWithoutExtension(file);
    //     String fileExtension = getFileExtension(file);
    //
    //     System.out.println("File Name (without extension): " + fileName);
    //     System.out.println("File Extension: " + fileExtension);
    //
    //     // ... further processing
    //     return ResponseEntity.ok("File uploaded successfully!");
    // }
}