package com.jb.service;

import com.jb.entity.ImageEntity;
import com.jb.repo.ImageRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final ImageRepo imageRepo;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    public S3Service(S3Client s3Client, ImageRepo imageRepo) {
        this.s3Client = s3Client;
        this.imageRepo = imageRepo;
    }

    public String uploadFile(MultipartFile file) throws IOException {

        System.out.println("Bucket name: " + bucketName);
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).
                key(fileName).contentType(file.getContentType()).build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
        ImageEntity imageEntity = ImageEntity.builder().imageName(fileName).imageUrl(fileUrl).build();
        imageRepo.save(imageEntity);
        return fileUrl;
    }

    public List<ImageEntity> getAllImages() {
        return imageRepo.findAll();
    }

}
