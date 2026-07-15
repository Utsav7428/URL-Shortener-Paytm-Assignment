package com.paytm.url_shortener.service;

import com.paytm.url_shortener.dto.ClickDetailsDto;
import com.paytm.url_shortener.dto.LinkAnalyticsResponse;
import com.paytm.url_shortener.dto.ShortenRequest;
import com.paytm.url_shortener.dto.ShortenResponse;
import com.paytm.url_shortener.exception.AliasAlreadyExistsException;
import com.paytm.url_shortener.exception.UrlNotFoundException;
import com.paytm.url_shortener.model.UrlMapping;
import com.paytm.url_shortener.repository.ClickAnalyticRepository;
import com.paytm.url_shortener.repository.UrlMappingRepository;
import com.paytm.url_shortener.util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;
    private final String domainPrefix;
    private final ClickAnalyticRepository clickRepository;

    public UrlShortenerService(UrlMappingRepository repository,
                               @Value("${app.domain-prefix:http://localhost:8080/}") String domainPrefix, ClickAnalyticRepository clickRepository) {
        this.repository = repository;
        this.domainPrefix = domainPrefix.endsWith("/") ? domainPrefix : domainPrefix + "/";
        this.clickRepository = clickRepository;
    }

    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest request) {
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            String alias = request.getCustomAlias().trim();

            if (repository.findByShortCode(alias).isPresent()) {
                throw new AliasAlreadyExistsException("Custom alias '" + alias + "' is already taken.");
            }

            UrlMapping mapping = UrlMapping.builder()
                    .originalUrl(request.getUrl())
                    .shortCode(alias)
                    .isCustom(true)
                    .build();

            UrlMapping saved = repository.save(mapping);
            return mapToResponse(saved);
        }

        return repository.findByOriginalUrlAndIsCustomFalse(request.getUrl())
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    Long nextId = repository.getNextId();
                    String generatedCode = Base62.encode(nextId);

                    UrlMapping mapping = UrlMapping.builder()
                            .id(nextId)
                            .originalUrl(request.getUrl())
                            .shortCode(generatedCode)
                            .isCustom(false)
                            .build();

                    UrlMapping saved = repository.save(mapping);
                    return mapToResponse(saved);
                });
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with code '" + shortCode + "' not found."));
    }

    private ShortenResponse mapToResponse(UrlMapping mapping) {
        return ShortenResponse.builder()
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(domainPrefix + mapping.getShortCode())
                .isCustom(mapping.isCustom())
                .createdAt(mapping.getCreatedAt())
                .build();
    }


    @Transactional(readOnly = true)
    public List<ShortenResponse> getAllMappings() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LinkAnalyticsResponse getLinkAnalytics(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with code '" + shortCode + "' not found."));

        long count = clickRepository.countByUrlMapping(mapping);

        List<ClickDetailsDto> recentClicks = clickRepository.findByUrlMappingOrderByClickedAtDesc(mapping, PageRequest.of(0, 10))
                .stream()
                .map(click -> ClickDetailsDto.builder()
                        .ipAddress(click.getIpAddress())
                        .userAgent(click.getUserAgent())
                        .referer(click.getReferer())
                        .clickedAt(click.getClickedAt())
                        .build())
                .collect(Collectors.toList());

        return LinkAnalyticsResponse.builder()
                .shortCode(shortCode)
                .originalUrl(mapping.getOriginalUrl())
                .totalClicks(count)
                .recentClicks(recentClicks)
                .build();
    }

    @Transactional(readOnly = true)
    public UrlMapping getMappingEntity(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with code '" + shortCode + "' not found."));
    }
}