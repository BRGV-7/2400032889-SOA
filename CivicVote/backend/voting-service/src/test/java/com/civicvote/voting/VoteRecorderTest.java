package com.civicvote.voting;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
class VoteRecorderTest {
    private final ParticipationRepository participations=mock(ParticipationRepository.class);
    private final BallotRepository ballots=mock(BallotRepository.class);
    private final VoteRecorder recorder=new VoteRecorder(participations,ballots);

    @Test @SuppressWarnings("null") void persistsParticipationAndAnonymousBallot(){
        recorder.record(4L,8L,12L);
        verify(participations).saveAndFlush(any(VoteParticipation.class));
        verify(ballots).saveAndFlush(any(AnonymousBallot.class));
    }

    @Test @SuppressWarnings("null") void rejectsDuplicateBeforeWritingEitherRecord(){
        when(participations.existsByElectionIdAndVoterId(4L,12L)).thenReturn(true);
        assertThrows(ResponseStatusException.class,()->recorder.record(4L,8L,12L));
        verify(participations,never()).saveAndFlush(any(VoteParticipation.class));
        verifyNoInteractions(ballots);
    }

    @Test @SuppressWarnings("null") void translatesConcurrentUniqueConstraintViolation(){
        doThrow(new DataIntegrityViolationException("unique key"))
            .when(participations).saveAndFlush(any(VoteParticipation.class));
        assertThrows(ResponseStatusException.class,()->recorder.record(4L,8L,12L));
        verify(ballots,never()).saveAndFlush(any(AnonymousBallot.class));
    }
}
