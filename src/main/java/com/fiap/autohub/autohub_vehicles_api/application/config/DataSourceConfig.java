package com.fiap.autohub.autohub_vehicles_api.application.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    @Profile("!local & !test")
    public DataSource dataSource(
            DataSourceProperties properties,
            @Value("${aws.region}") String awsRegion,
            @Value("${DB_PASSWORD_SECRET_ARN}") String secretArn) {
        logger.info("getting password db. AWS Region: {}, Secret ARN: {}", awsRegion, secretArn);

        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretArn)
                .build();

        GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
        String secretPassword = valueResponse.secretString();

        logger.info("got password db");

        // Cria e configura o DataSource Hikari
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        dataSource.setPassword(secretPassword);

        secretsClient.close();
        return dataSource;
    }
}