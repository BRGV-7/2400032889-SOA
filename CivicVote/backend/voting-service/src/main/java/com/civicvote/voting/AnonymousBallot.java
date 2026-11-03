package com.civicvote.voting;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="anonymous_ballots",indexes=@Index(name="idx_ballot_election_candidate",columnList="election_id,candidate_id"))
public class AnonymousBallot {
    @Id @Column(length=36) private String id;
    @Column(name="election_id",nullable=false) private Long electionId;
    @Column(name="candidate_id",nullable=false) private Long candidateId;
    protected AnonymousBallot(){}
    public AnonymousBallot(Long electionId,Long candidateId){this.id=UUID.randomUUID().toString();this.electionId=electionId;this.candidateId=candidateId;}
}
