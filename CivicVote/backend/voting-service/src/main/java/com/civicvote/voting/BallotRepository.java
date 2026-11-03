package com.civicvote.voting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface BallotRepository extends JpaRepository<AnonymousBallot,String> {
    long deleteByElectionId(Long electionId);
    @Query("select b.candidateId, count(b) from AnonymousBallot b where b.electionId=:electionId group by b.candidateId")
    List<Object[]> tally(@Param("electionId") Long electionId);
}
