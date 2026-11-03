CREATE DATABASE IF NOT EXISTS civicvote_election_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE civicvote_election_db;
CREATE TABLE IF NOT EXISTS elections (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  description VARCHAR(2000),
  starts_at TIMESTAMP NOT NULL,
  ends_at TIMESTAMP NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
  CONSTRAINT chk_election_status CHECK (status IN ('DRAFT','SCHEDULED','OPEN','CLOSED')),
  CONSTRAINT chk_election_dates CHECK (ends_at > starts_at)
);
CREATE TABLE IF NOT EXISTS candidates (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  election_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  party VARCHAR(120),
  manifesto VARCHAR(2000),
  CONSTRAINT fk_candidate_election FOREIGN KEY (election_id) REFERENCES elections(id) ON DELETE CASCADE,
  CONSTRAINT uq_candidate_election_name UNIQUE (election_id, name),
  INDEX idx_candidate_election (election_id)
);
