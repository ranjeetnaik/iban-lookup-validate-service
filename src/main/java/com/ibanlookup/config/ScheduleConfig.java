package com.ibanlookup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile({"local-localstack", "dev", "ci", "stg", "prod"})
public class ScheduleConfig {
}