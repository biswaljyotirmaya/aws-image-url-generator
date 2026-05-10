package com.jb.congfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.access-key}")
    private String accessKey;

    @Value("${cloud.aws.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        if (!isLoaded(accessKey) || !isLoaded(secretKey)) {
            throw new IllegalStateException("AWS credentials are not loaded. Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY in the system environment.");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public CommandLineRunner awsCredentialCheck() {
        return args -> {
            System.out.println("AWS access key loaded: " + isLoaded(accessKey) + " (" + mask(accessKey) + ")");
            System.out.println("AWS secret key loaded: " + isLoaded(secretKey) + " (" + mask(secretKey) + ")");
            System.out.println("AWS region loaded: " + isLoaded(region) + " (" + region + ")");
        };
    }

    private boolean isLoaded(String value) {
        return value != null && !value.isBlank() && !value.startsWith("${");
    }

    private String mask(String value) {
        if (!isLoaded(value)) {
            return "not loaded";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 4) + "****";
    }

}
