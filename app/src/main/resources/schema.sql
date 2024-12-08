DROP TABLE IF EXISTS urls;

CREATE TABLE URLS (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    protocol VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port integer,
    created_at TIMESTAMP
);

CREATE INDEX urls_created_at
    ON URLS (created_at ASC);
