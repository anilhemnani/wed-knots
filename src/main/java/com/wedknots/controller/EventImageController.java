package com.wedknots.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class EventImageController {
    @GetMapping("/event-image/{eventId}/{filename:.+}")
    public ResponseEntity<Resource> getEventImage(@PathVariable String eventId, @PathVariable String filename) {
        String resourcePath = "templates/events/" + eventId + "/" + filename;
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
        else if (lower.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
