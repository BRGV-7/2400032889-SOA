package com.civicvote.voting;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class VoteService {
    private final ParticipationRepository participations; private final ElectionVerifier verifier; private final VoteRecorder recorder;
    public VoteService(ParticipationRepository participations,ElectionVerifier verifier,VoteRecorder recorder){this.participations=participations;this.verifier=verifier;this.recorder=recorder;}
    public void cast(Long electionId,Long candidateId,Long voterId,String token){
        if(participations.existsByElectionIdAndVoterId(electionId,voterId))throw new ResponseStatusException(HttpStatus.CONFLICT,"A vote has already been recorded for this election");
        ElectionVerifier.ElectionInfo election=verifier.verify(electionId,candidateId,token);
        if(!"OPEN".equals(election.status()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Voting is not currently open");
        if(!election.candidateMember())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Candidate does not belong to this election");
        recorder.record(electionId,candidateId,voterId);
    }
}
