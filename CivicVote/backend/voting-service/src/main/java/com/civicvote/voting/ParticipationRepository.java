package com.civicvote.voting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ParticipationRepository extends JpaRepository<VoteParticipation,Long> {
    boolean existsByElectionIdAndVoterId(Long electionId,Long voterId);
    long deleteByElectionId(Long electionId);
}
