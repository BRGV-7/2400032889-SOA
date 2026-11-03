package com.civicvote.result;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="result_publications")
public class ResultPublication {
    @Id @Column(name="election_id") private Long electionId;
    @Column(name="published_at",nullable=false) private Instant publishedAt=Instant.now();
    @Column(name="published_by",nullable=false) private Long publishedBy;
    protected ResultPublication(){}
    public ResultPublication(Long electionId,Long publishedBy){this.electionId=electionId;this.publishedBy=publishedBy;}
    public Instant getPublishedAt(){return publishedAt;}
}
