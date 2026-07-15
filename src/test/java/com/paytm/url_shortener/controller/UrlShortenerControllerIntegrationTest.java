package com.paytm.url_shortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.url_shortener.dto.ShortenRequest;
import com.paytm.url_shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/shorten - Should successfully create short URL")
    void shortenUrlSuccess() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("https://www.github.com")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl", is("https://www.github.com")))
                .andExpect(jsonPath("$.shortCode", notNullValue()))
                .andExpect(jsonPath("$.shortUrl", containsString("http://localhost:8080/")))
                .andExpect(jsonPath("$.custom", is(false)));
    }

    @Test
    @DisplayName("POST /api/v1/shorten - Should return 400 Bad Request for invalid URL")
    void shortenUrlInvalidUrl() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("not-a-valid-url")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.validationErrors.url", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/shorten - Should handle custom alias and throw 409 Conflict if taken")
    void shortenUrlCustomAliasConflict() throws Exception {
        ShortenRequest firstRequest = ShortenRequest.builder()
                .url("https://www.google.com")
                .customAlias("google")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        ShortenRequest duplicateRequest = ShortenRequest.builder()
                .url("https://www.alphabet.com")
                .customAlias("google")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    @DisplayName("GET /{shortCode} - Should redirect with 301 Moved Permanently to original URL")
    void redirectSuccess() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("https://www.wikipedia.org")
                .customAlias("wiki")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/wiki"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("https://www.wikipedia.org"));
    }

    @Test
    @DisplayName("GET /{shortCode} - Should return 404 Not Found for missing shortcode")
    void redirectNotFound() throws Exception {
        mockMvc.perform(get("/unknownCode"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}