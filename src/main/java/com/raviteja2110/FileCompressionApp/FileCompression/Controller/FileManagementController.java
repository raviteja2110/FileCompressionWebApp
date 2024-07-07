package com.raviteja2110.FileCompressionApp.FileCompression.Controller;

import com.raviteja2110.FileCompressionApp.FileCompression.Model.FileInfo;
import com.raviteja2110.FileCompressionApp.FileCompression.Service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class FileManagementController {

    @Autowired
    private GoogleDriveService googleDriveService;

    @GetMapping("/listFiles")
    @ResponseBody
    public void listFiles() {
        try {
            List<FileInfo> files = googleDriveService.listFiles();
        } catch (IOException e) {
            System.out.println("message Failed to list files: " + e.getMessage());
        }
    }

    @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileId) {
        try {
            FileInfo fileInfo = googleDriveService.getFileInfo(fileId);
            Resource resource = googleDriveService.downloadFile(fileId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{fileId}")
    @ResponseBody
    public ResponseEntity<String> deleteFile(@PathVariable("fileId") String fileId) {
        try {
            googleDriveService.deleteFile(fileId);
            return ResponseEntity.ok("File deletion successful");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File deletion failed: " + e.getMessage());
        }
    }
}
