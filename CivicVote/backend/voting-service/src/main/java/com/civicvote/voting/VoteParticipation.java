package com.civicvote.voting;
import jakarta.persistence.*;
@Entity @Table(name="vote_participations",uniqueConstraints=@UniqueConstraint(name="uq_one_participation",columnNames={"election_id","voter_id"}))
public class VoteParticipation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="election_id",nullable=false) private Long electionId;
    @Column(name="voter_id",nullable=false) private Long voterId;
    protected VoteParticipation(){}
    public VoteParticipation(Long electionId,Long voterId){this.electionId=electionId;this.voterId=voterId;}
}
