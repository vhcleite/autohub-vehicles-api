package com.fiap.autohub.autohub_vehicles_api.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

@Configuration
public class SnsConfig {
    @Bean
    @Profile("!local & !test")
    public SnsClient snsClientProd(@Value("${aws.region}") String awsRegion) {
        return SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("local")
    public SnsClient snsClientLocal(
            @Value("${aws.region}") String awsRegion,
            @Value("${aws.localstack.endpoint}") String localstackEndpoint, // Endpoint do LocalStack
            @Value("${aws.credentials.accessKey}") String accessKey, // Credenciais dummy
            @Value("${aws.credentials.secretKey}") String secretKey) {
        return SnsClient.builder()
                .region(Region.of(awsRegion))
                .endpointOverride(URI.create(localstackEndpoint)) // Aponta para LocalStack
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
