package com.example.webapp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
public class S3Service {

    private final String bucketName;
    private final String region;
    private final S3Client s3Client;

    @Autowired
    public S3Service(@Value("${aws.s3.bucket-name}") String bucketName,
                     @Value("${aws.s3.region}") String region) {
        this.bucketName = bucketName;
        this.region = region;

        // Construim S3Client folosind DefaultCredentialsProvider
        this.s3Client = S3Client.builder()
                .region(Region.of(region))  // Setăm regiunea corectă pentru bucket-ul S3
                .credentialsProvider(DefaultCredentialsProvider.create())  // Folosim credențialele din mediul înconjurător
                .build();
    }

    // Metodă pentru a încărca un fișier nou
    public String uploadFile(InputStream inputStream, String originalFileName) throws Exception {
        String key = UUID.randomUUID().toString() + "_" + originalFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    // Metodă pentru a șterge fișierul vechi
    public void deleteFile(String imageUrl) throws Exception {
        String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1); // Extragem cheia fișierului din URL

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
