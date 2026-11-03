package com.civicvote.voting;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
class VoteServiceTest {
    private final ParticipationRepository participations=mock(ParticipationRepository.class);
    private final BallotRepository ballots=mock(BallotRepository.class);
    private final ElectionVerifier verifier=mock(ElectionVerifier.class);
    private final VoteService service=new VoteService(participations,verifier,new VoteRecorder(participations,ballots));
    @Test @SuppressWarnings("null") void recordsVoteOnlyWhenElectionAllowsVote(){
        when(verifier.verify(4L,8L,"******")).thenReturn(new ElectionVerifier.ElectionInfo("OPEN",true));
        service.cast(4L,8L,12L,"******");
        verify(participations).saveAndFlush(any(VoteParticipation.class));
        verify(ballots).saveAndFlush(any(AnonymousBallot.class));
    }
    @Test @SuppressWarnings("null") void recordsNotaVoteUsingTheSystemCandidateId(){
        when(verifier.verify(4L,-1L,"******")).thenReturn(new ElectionVerifier.ElectionInfo("OPEN",true));
        service.cast(4L,-1L,12L,"******");
        verify(participations).saveAndFlush(any(VoteParticipation.class));
        verify(ballots).saveAndFlush(any(AnonymousBallot.class));
    }
    @Test @SuppressWarnings("null") void rejectsDuplicateParticipationBeforeValidation(){
        when(participations.existsByElectionIdAndVoterId(4L,12L)).thenReturn(true);
        assertThrows(ResponseStatusException.class,()->service.cast(4L,8L,12L,"******"));
        verifyNoInteractions(verifier);
        verify(participations,never()).saveAndFlush(any(VoteParticipation.class));
        verifyNoInteractions(ballots);
    }
    @Test @SuppressWarnings("null") void doesNotRecordWhenElectionIsClosed(){
        when(verifier.verify(4L,8L,"******")).thenReturn(new ElectionVerifier.ElectionInfo("CLOSED",true));
        assertThrows(ResponseStatusException.class,()->service.cast(4L,8L,12L,"******"));
        verify(participations,never()).saveAndFlush(any(VoteParticipation.class));
        verifyNoInteractions(ballots);
    }
    @Test @SuppressWarnings("null") void doesNotRecordWhenCandidateDoesNotBelongToElection(){
        when(verifier.verify(4L,8L,"******")).thenReturn(new ElectionVerifier.ElectionInfo("OPEN",false));
        assertThrows(ResponseStatusException.class,()->service.cast(4L,8L,12L,"******"));
        verify(participations,never()).saveAndFlush(any(VoteParticipation.class));
        verifyNoInteractions(ballots);
    }
}
