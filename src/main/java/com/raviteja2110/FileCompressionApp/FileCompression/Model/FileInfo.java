package com.raviteja2110.FileCompressionApp.FileCompression.Model;

import lombok.Data;

@Data
public class FileInfo {
    private String id;
    private String name;
    private String downloadLink;

    public FileInfo(String id, String name, String downloadLink) {
        this.id = id;
        this.name = name;
        this.downloadLink = downloadLink;
    }

}