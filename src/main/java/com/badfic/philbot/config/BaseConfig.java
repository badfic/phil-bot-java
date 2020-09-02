package com.badfic.philbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseConfig {

    @Value("${OWNER_ID}")
    public String ownerId;

    @Value("${NODE_ENV:test}")
    public String nodeEnvironment;

    @Value("${GFYCAT_CLIENT_ID:}")
    public String gfycatClientId;

    @Value("${GFYCAT_CLIENT_SECRET:}")
    public String gfycatClientSecret;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
