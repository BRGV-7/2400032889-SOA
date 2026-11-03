package com.civicvote.voting;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties={
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "security.jwt.secret=integration-test-secret-at-least-32-bytes-long",
    "security.internal-key=integration-test-internal-key"
})
@EnabledIfSystemProperty(named="civicvote.mysql.integration",matches="true")
class MySqlVotePersistenceIT {
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url",()->System.getProperty("civicvote.mysql.url","jdbc:mysql://localhost:3306/civicvote_voting_test_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        properties.add("spring.datasource.username",()->System.getProperty("civicvote.mysql.username","root"));
        properties.add("spring.datasource.password",()->System.getProperty("civicvote.mysql.password",""));
    }
    @Autowired VoteService service;
    @Autowired ParticipationRepository participations;
    @Autowired BallotRepository ballots;
    @MockitoBean ElectionVerifier verifier;
    @MockitoSpyBean BallotRepository ballotSpy;
    private Long electionId;
    private Long voterId;
    @BeforeEach void prepare(){
        electionId=System.currentTimeMillis();
        voterId=electionId+1;
        when(verifier.verify(eq(electionId),anyLong(),anyString())).thenReturn(new ElectionVerifier.ElectionInfo("OPEN",true));
    }
    @AfterEach void clean(){
        ballots.deleteByElectionId(electionId);
        participations.deleteByElectionId(electionId);
    }
    @Test void committedVotePersistsSeparateParticipationAndAnonymousBallot(){
        service.cast(electionId,55L,voterId,"Bearer integration");
        assertTrue(participations.existsByElectionIdAndVoterId(electionId,voterId));
        assertEquals(1,ballots.tally(electionId).stream().mapToLong(row->((Number)row[1]).longValue()).sum());
    }
    @Test @SuppressWarnings("null") void ballotFailureRollsBackParticipation(){
        doThrow(new DataIntegrityViolationException("simulated ballot persistence failure")).when(ballotSpy).saveAndFlush(any(AnonymousBallot.class));
        assertThrows(ResponseStatusException.class,()->service.cast(electionId,55L,voterId,"Bearer integration"));
        assertFalse(participations.existsByElectionIdAndVoterId(electionId,voterId));
    }
    @Test void concurrentDuplicateRequestsCommitOnlyOneBallot()throws Exception{
        CountDownLatch bothChecked=new CountDownLatch(2);
        when(verifier.verify(eq(electionId),eq(55L),anyString())).thenAnswer(invocation->{
            bothChecked.countDown();
            if(!bothChecked.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Concurrent vote requests did not synchronize");
            return new ElectionVerifier.ElectionInfo("OPEN",true);
        });
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> cast=()->{try{service.cast(electionId,55L,voterId,"Bearer integration");return true;}catch(ResponseStatusException ex){return false;}};
            Future<Boolean> first=pool.submit(cast);Future<Boolean> second=pool.submit(cast);
            assertNotEquals(first.get(15,TimeUnit.SECONDS),second.get(15,TimeUnit.SECONDS));
            assertEquals(1,ballots.tally(electionId).stream().mapToLong(row->((Number)row[1]).longValue()).sum());
        } finally { pool.shutdownNow(); }
    }
}
