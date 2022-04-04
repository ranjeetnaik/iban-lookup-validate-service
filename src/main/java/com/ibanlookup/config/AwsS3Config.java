package com.ibanlookup.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local-localstack", "dev", "ci", "stg", "prod"})
public class AwsS3Config {

    @Value("${s3.url:}")
    private String s3EndpointUrl;
    @Value("${s3.region:eu-west-1}")
    private String s3Region;

    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationS3());
        builder.setPathStyleAccessEnabled(true);
        return builder.build();
    }

    private AwsClientBuilder.EndpointConfiguration getEndpointConfigurationS3() {
        return new AwsClientBuilder.EndpointConfiguration(s3EndpointUrl, s3Region);
    }


}