package com.paytm.url_shortener.repository;

import com.paytm.url_shortener.model.ClickAnalytic;
import com.paytm.url_shortener.model.UrlMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticRepository extends JpaRepository<ClickAnalytic, Long> {

    long countByUrlMapping(UrlMapping urlMapping);

    List<ClickAnalytic> findByUrlMappingOrderByClickedAtDesc(UrlMapping urlMapping, Pageable pageable);
}