package com.civicvote.election;
import jakarta.persistence.*;
@Entity @Table(name="candidates",uniqueConstraints=@UniqueConstraint(name="uq_candidate_election_name",columnNames={"election_id","name"}))
public class Candidate {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="election_id",nullable=false) private Election election;
    @Column(nullable=false,length=120) private String name;
    @Column(length=120) private String party;
    @Column(length=2000) private String manifesto;
    protected Candidate(){}
    public Candidate(Election election,String name,String party,String manifesto){this.election=election;this.name=name;this.party=party;this.manifesto=manifesto;}
    public Long getId(){return id;} public String getName(){return name;} public String getParty(){return party;} public String getManifesto(){return manifesto;}
}
