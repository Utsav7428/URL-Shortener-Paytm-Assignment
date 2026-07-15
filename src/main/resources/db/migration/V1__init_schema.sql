CREATE TABLE url_mappings (
                              id BIGSERIAL PRIMARY KEY,
                              original_url TEXT NOT NULL,
                              short_code VARCHAR(100) UNIQUE NOT NULL,
                              is_custom BOOLEAN DEFAULT FALSE NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Index for fast lookups when validating duplicate URLs
CREATE INDEX idx_url_mappings_original_url ON url_mappings(original_url);

-- Index for ultra-fast redirection lookups
CREATE INDEX idx_url_mappings_short_code ON url_mappings(short_code);