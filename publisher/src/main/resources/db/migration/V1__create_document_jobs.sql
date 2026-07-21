CREATE TABLE document_jobs (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    status VARCHAR(32) NOT NULL,
    extracted_text TEXT,
    error_code VARCHAR(64),
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_document_jobs_status ON document_jobs(status);
