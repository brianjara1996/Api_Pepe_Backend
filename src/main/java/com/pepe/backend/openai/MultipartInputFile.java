package com.pepe.backend.openai;

import org.springframework.core.io.ByteArrayResource;

public class MultipartInputFile extends ByteArrayResource {

    private final String filename;
    private final String contentType;

    public MultipartInputFile(String filename, byte[] byteArray, String contentType) {
        super(byteArray);
        this.filename = filename == null ? "audio.webm" : filename;
        this.contentType = contentType == null ? "audio/webm" : contentType;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}