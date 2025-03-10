package com.daicy.devtools.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonConfig {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
} 