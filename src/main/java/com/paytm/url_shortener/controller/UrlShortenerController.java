package com.paytm.url_shortener.controller;

import com.paytm.url_shortener.dto.ShortenRequest;
import com.paytm.url_shortener.dto.ShortenResponse;
import com.paytm.url_shortener.dto.LinkAnalyticsResponse;
import com.paytm.url_shortener.model.UrlMapping;
import com.paytm.url_shortener.service.UrlShortenerService;
import com.paytm.url_shortener.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping
public class UrlShortenerController {

    private final UrlShortenerService service;
    private final AnalyticsService analyticsService;

    public UrlShortenerController(UrlShortenerService service, AnalyticsService analyticsService) {
        this.service = service;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.shortenUrl(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/links")
    public ResponseEntity<List<ShortenResponse>> getAllLinks() {
        return ResponseEntity.ok(service.getAllMappings());
    }

    @GetMapping("/api/v1/analytics/{shortCode}")
    public ResponseEntity<LinkAnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getLinkAnalytics(shortCode));
    }

    // Consolidated redirect endpoint with asynchronous analytics logging
    @GetMapping("/{shortCode:[a-zA-Z0-9\\-_]+}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode, HttpServletRequest request) {
        UrlMapping mapping = service.getMappingEntity(shortCode);

        // Extract caller metadata
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }

        // Fire off asynchronous, non-blocking click logging task
        analyticsService.recordClickAsync(mapping, userAgent, referer, ipAddress);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(mapping.getOriginalUrl());
        return redirectView;
    }
}