package com.paytm.url_shortener.service;

import com.paytm.url_shortener.dto.ShortenRequest;
import com.paytm.url_shortener.dto.ShortenResponse;
import com.paytm.url_shortener.exception.AliasAlreadyExistsException;
import com.paytm.url_shortener.exception.UrlNotFoundException;
import com.paytm.url_shortener.model.UrlMapping;
import com.paytm.url_shortener.repository.UrlMappingRepository;
import com.paytm.url_shortener.util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;
    private final String domainPrefix;

    public UrlShortenerService(UrlMappingRepository repository,
                               @Value("${app.domain-prefix:http://localhost:8080/}") String domainPrefix) {
        this.repository = repository;
        this.domainPrefix = domainPrefix.endsWith("/") ? domainPrefix : domainPrefix + "/";
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
}