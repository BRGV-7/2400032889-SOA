package com.civicvote.voting;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoteRecorder {
    private final ParticipationRepository participations;
    private final BallotRepository ballots;

    public VoteRecorder(ParticipationRepository participations, BallotRepository ballots) {
        this.participations = participations;
        this.ballots = ballots;
    }

    @Transactional
    public void record(Long electionId, Long candidateId, Long voterId) {
        if (participations.existsByElectionIdAndVoterId(electionId, voterId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A vote has already been recorded for this election");
        }
        try {
            participations.saveAndFlush(new VoteParticipation(electionId, voterId));
            ballots.saveAndFlush(new AnonymousBallot(electionId, candidateId));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A vote has already been recorded for this election", ex);
        }
    }
}
