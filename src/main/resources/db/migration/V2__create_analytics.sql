CREATE TABLE click_analytics (
                                 id BIGSERIAL PRIMARY KEY,
                                 url_mapping_id BIGINT NOT NULL,
                                 clicked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 user_agent TEXT,
                                 referer TEXT,
                                 ip_address VARCHAR(45), -- Supports IPv6 length
                                 CONSTRAINT fk_click_analytics_url_mapping FOREIGN KEY (url_mapping_id) REFERENCES url_mappings(id) ON DELETE CASCADE
);

-- Index to optimize querying analytics for a specific short link
CREATE INDEX idx_click_analytics_mapping_id ON click_analytics(url_mapping_id);