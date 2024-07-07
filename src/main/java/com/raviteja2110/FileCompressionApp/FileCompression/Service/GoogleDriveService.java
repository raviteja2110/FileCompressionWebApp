package com.raviteja2110.FileCompressionApp.FileCompression.Service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.raviteja2110.FileCompressionApp.FileCompression.Model.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GoogleDriveService {

    @Autowired
    private Drive drive;

    @Async
    public CompletableFuture<String> uploadFileAsync(MultipartFile file) throws IOException {
        // Compress the file
        java.io.File compressedFile = compressFile(file);

        // Upload the compressed file
        File fileMetadata = new File();
        fileMetadata.setName(compressedFile.getName());

        try (InputStream inputStream = new FileInputStream(compressedFile)) {
            com.google.api.client.http.InputStreamContent mediaContent = new com.google.api.client.http.InputStreamContent("application/zip", inputStream);

            File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            // Set file to be publicly readable
            drive.permissions().create(uploadedFile.getId(), new Permission().setType("anyone").setRole("reader")).execute();

            // Clean up the temporary compressed file
            compressedFile.delete();

            return CompletableFuture.completedFuture(uploadedFile.getId());
        }
    }
    @Async
    public void scheduleFileDeletion(String fileId) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            try {
                deleteFile(fileId);
                System.out.println("File with ID " + fileId + " deleted successfully.");
            } catch (IOException e) {
                System.out.println("Failed to delete file with ID " + fileId + ": " + e.getMessage());
            } finally {
                executorService.shutdown();
            }
        }, 1, TimeUnit.MINUTES);
    }
    private java.io.File compressFile(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        java.io.File compressedFile = new java.io.File(tempDir, file.getOriginalFilename() + ".zip");

        try (FileOutputStream fos = new FileOutputStream(compressedFile);
             ZipOutputStream zipOut = new ZipOutputStream(fos);
             InputStream fis = file.getInputStream()) {

            ZipEntry zipEntry = new ZipEntry(file.getOriginalFilename());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }

        return compressedFile;
    }

    public List<FileInfo> listFiles() throws IOException {
        List<FileInfo> fileInfoList = new ArrayList<>();
        FileList result = drive.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, webContentLink)")
                .execute();
        for (File file : result.getFiles()) {
            fileInfoList.add(new FileInfo(file.getId(), file.getName(), file.getWebContentLink()));
            System.out.printf("Found file: %s (%s)\n", file.getName(), file.getId());
        }
        return fileInfoList;
    }

    public FileInfo getFileInfo(String fileId) throws IOException {
        File file = drive.files().get(fileId).setFields("id, name, webContentLink").execute();
        return new FileInfo(file.getId(), file.getName(), file.getWebContentLink());
    }

    public Resource downloadFile(String fileId) throws IOException {
        InputStream inputStream = drive.files().get(fileId).executeMediaAsInputStream();
        return new InputStreamResource(inputStream);
    }

    public void deleteFile(String fileId) throws IOException {
        drive.files().delete(fileId).execute();
    }
}
