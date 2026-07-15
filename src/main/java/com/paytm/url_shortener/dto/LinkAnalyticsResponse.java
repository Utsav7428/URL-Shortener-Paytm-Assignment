package com.paytm.url_shortener.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class LinkAnalyticsResponse {
    private final String shortCode;
    private final String originalUrl;
    private final long totalClicks;
    private final List<ClickDetailsDto> recentClicks;
}