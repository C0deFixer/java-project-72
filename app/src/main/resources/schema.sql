DROP TABLE IF EXISTS urls;

CREATE TABLE urls
(
    --id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    id         INT PRIMARY KEY AUTO_INCREMENT,
    protocol   VARCHAR(255) NOT NULL,
    host       VARCHAR(255) NOT NULL,
    port       INT,
    created_at TIMESTAMP
);

CREATE INDEX urls_created_at ON urls (created_at);
