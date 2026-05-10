package com.jb.controller;

import com.jb.entity.ImageEntity;
import com.jb.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean confirm
    ) throws Exception {
        if (!confirm) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Please confirm image upload by sending confirm=true.");
        }
        return ResponseEntity.ok(
                s3Service.uploadFile(file)
        );
    }

    @GetMapping
    public ResponseEntity<List<ImageEntity>> getAllImages() {
        return ResponseEntity.ok(
                s3Service.getAllImages()
        );
    }
}
