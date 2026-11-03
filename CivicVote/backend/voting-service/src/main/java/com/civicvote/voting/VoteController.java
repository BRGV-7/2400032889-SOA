package com.civicvote.voting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/votes")
public class VoteController {
    private final VoteService votes; private final ParticipationRepository participations; private final BallotRepository ballots; private final String internalKey;
    public VoteController(VoteService votes,ParticipationRepository participations,BallotRepository ballots,@Value("${security.internal-key}")String internalKey){this.votes=votes;this.participations=participations;this.ballots=ballots;this.internalKey=internalKey;}
    public record VoteInput(@NotNull Long electionId,@NotNull Long candidateId){}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> cast(@Valid @RequestBody VoteInput input,Authentication authentication,@RequestHeader("Authorization")String authorization){
        Long voterId=parseVoterId(authentication);votes.cast(input.electionId(),input.candidateId(),voterId,authorization);
        return Map.of("message","Ballot recorded");
    }
    @GetMapping("/status/{electionId}")
    public Map<String,Boolean> status(@PathVariable Long electionId,Authentication authentication){
        return Map.of("hasVoted",participations.existsByElectionIdAndVoterId(electionId,parseVoterId(authentication)));
    }
    @GetMapping("/internal/{electionId}/tally") @Transactional(readOnly=true)
    public Map<Long,Long> tally(@PathVariable Long electionId,@RequestHeader(value="X-Internal-Key",required=false)String key){
        if(!internalKey.equals(key))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Internal access required");
        return ballots.tally(electionId).stream().collect(java.util.stream.Collectors.toMap(r->((Number)r[0]).longValue(),r->((Number)r[1]).longValue()));
    }
    private Long parseVoterId(Authentication authentication){try{return Long.parseLong(authentication.getName());}catch(NumberFormatException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid voter identity",ex);}}
}
