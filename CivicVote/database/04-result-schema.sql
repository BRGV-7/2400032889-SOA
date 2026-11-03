CREATE DATABASE IF NOT EXISTS civicvote_result_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE civicvote_result_db;
CREATE TABLE IF NOT EXISTS result_publications (
  election_id BIGINT NOT NULL PRIMARY KEY,
  published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_by BIGINT NOT NULL
);
