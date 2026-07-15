package com.paytm.url_shortener.service;

import com.paytm.url_shortener.model.ClickAnalytic;
import com.paytm.url_shortener.model.UrlMapping;
import com.paytm.url_shortener.repository.ClickAnalyticRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final ClickAnalyticRepository clickRepository;

    public AnalyticsService(ClickAnalyticRepository clickRepository) {
        this.clickRepository = clickRepository;
    }

    @Async
    @Transactional
    public void recordClickAsync(UrlMapping mapping, String userAgent, String referer, String ipAddress) {
        ClickAnalytic analytic = ClickAnalytic.builder()
                .urlMapping(mapping)
                .userAgent(userAgent)
                .referer(referer)
                .ipAddress(ipAddress)
                .build();
        clickRepository.save(analytic);
    }
}