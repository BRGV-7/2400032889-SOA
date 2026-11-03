package com.civicvote.election;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class ElectionValidationTest {
    @Test void electionTimeBoundariesProduceScheduledOpenAndClosedStates(){
        Instant now=Instant.now();
        Election election=new Election("Town Council","",now.plusSeconds(60),now.plusSeconds(3600));
        assertEquals("SCHEDULED",election.getStatus());
    }
    @Test void electionIsOpenInsideItsConfiguredWindow(){
        Instant now=Instant.now();
        Election election=new Election("Town Council","",now.minusSeconds(60),now.plusSeconds(3600));
        election.getCandidates().add(new Candidate(election,"A","Party A",""));
        election.getCandidates().add(new Candidate(election,"B","Party B",""));
        assertEquals("OPEN",election.getStatus());
    }
}
