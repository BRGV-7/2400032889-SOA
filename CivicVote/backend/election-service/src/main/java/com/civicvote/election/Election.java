package com.civicvote.election;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="elections")
public class Election {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=160) private String title;
    @Column(length=2000) private String description;
    @Column(name="starts_at",nullable=false) private Instant startsAt;
    @Column(name="ends_at",nullable=false) private Instant endsAt;
    @Column(nullable=false,length=16) private String status="SCHEDULED";
    @OneToMany(mappedBy="election",cascade=CascadeType.ALL,orphanRemoval=true) private List<Candidate> candidates=new ArrayList<>();
    protected Election(){}
    public Election(String title,String description,Instant startsAt,Instant endsAt){this.title=title;this.description=description;this.startsAt=startsAt;this.endsAt=endsAt;}
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;}
    public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;}
    public String getStatus(){if("DRAFT".equals(status))return status;Instant now=Instant.now();if(now.isBefore(startsAt))return "SCHEDULED";if(now.isBefore(endsAt))return candidates.size()>=2?"OPEN":"SCHEDULED";return "CLOSED";}
    public List<Candidate> getCandidates(){return candidates;}
    public void update(String title,String description,Instant startsAt,Instant endsAt){this.title=title;this.description=description;this.startsAt=startsAt;this.endsAt=endsAt;}
}
