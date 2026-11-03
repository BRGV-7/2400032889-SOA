CREATE DATABASE IF NOT EXISTS civicvote_voting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE civicvote_voting_db;
-- This table records that a voter participated; it deliberately has no ballot identifier.
CREATE TABLE IF NOT EXISTS vote_participations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  election_id BIGINT NOT NULL,
  voter_id BIGINT NOT NULL,
  cast_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_one_participation UNIQUE (election_id, voter_id),
  INDEX idx_participation_election (election_id)
);
-- This table contains candidate selections only; it deliberately has no voter/participation identifier.
CREATE TABLE IF NOT EXISTS anonymous_ballots (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  election_id BIGINT NOT NULL,
  candidate_id BIGINT NOT NULL,
  INDEX idx_ballot_election_candidate (election_id, candidate_id)
);
