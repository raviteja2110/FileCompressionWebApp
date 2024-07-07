package com.raviteja2110.FileCompressionApp.FileCompression.Controller;

import com.raviteja2110.FileCompressionApp.FileCompression.Service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.*;

@Controller
public class UploadController {

    @Autowired
    private GoogleDriveService googleDriveService;

    @GetMapping("/")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        try {
            CompletableFuture<String> fileIdFuture = googleDriveService.uploadFileAsync(file);
            String fileId = fileIdFuture.get();
            googleDriveService.scheduleFileDeletion(fileId); // Schedule file deletion
            return ResponseEntity.ok().body("{\"fileId\": \"" + fileId + "\"}");
        } catch (IOException | InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body("{\"message\": \"File upload failed: " + e.getMessage() + "\"}");
        }
    }


    @GetMapping("/download-countdown/{fileId}")
    public String showDownloadCountdown(@PathVariable String fileId, Model model) {
        model.addAttribute("fileId", fileId);
        return "download-countdown";
    }
}
