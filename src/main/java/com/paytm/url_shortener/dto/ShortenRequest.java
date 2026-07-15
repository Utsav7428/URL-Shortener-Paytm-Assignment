package com.paytm.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "Please provide a valid URL")
    private String url;

    private String customAlias;
}