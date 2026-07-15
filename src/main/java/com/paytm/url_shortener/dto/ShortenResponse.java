package com.paytm.url_shortener.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenResponse {
    private String originalUrl;
    private String shortUrl;
    private String shortCode;
    private boolean isCustom;
    private Instant createdAt;
}