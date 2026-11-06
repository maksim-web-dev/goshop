CREATE TABLE IF NOT EXISTS search_documents (
    product_id    UUID        PRIMARY KEY,
    sku           VARCHAR(64) NOT NULL,
    name          VARCHAR(512) NOT NULL,
    brand         VARCHAR(128) NOT NULL,
    category_id   UUID        NOT NULL,
    searchable_text TSVECTOR,
    status        VARCHAR(32) NOT NULL,
    indexed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_search_documents_fts   ON search_documents USING GIN (searchable_text);
CREATE INDEX IF NOT EXISTS idx_search_documents_brand ON search_documents (brand);
CREATE INDEX IF NOT EXISTS idx_search_documents_cat   ON search_documents (category_id);
