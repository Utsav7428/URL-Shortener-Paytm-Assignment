package com.paytm.url_shortener.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
public class ClickDetailsDto {
    private final String ipAddress;
    private final String userAgent;
    private final String referer;
    private final Instant clickedAt;
}