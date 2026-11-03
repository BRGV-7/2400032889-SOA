package com.civicvote.result;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;
@RestController @RequestMapping("/api/results")
public class ResultController {
    private final PublicationRepository publications; private final ElectionDataClient elections;
    public ResultController(PublicationRepository publications,ElectionDataClient elections){this.publications=publications;this.elections=elections;}
    public record ResultView(Long electionId,String electionTitle,long totalVotes,List<ResultCalculator.CandidateTally> candidates,List<Long> winners,boolean tied){}
    @GetMapping("/{id}") @Transactional(readOnly=true)
    public ResultView get(@PathVariable @NonNull Long id,@RequestHeader(HttpHeaders.AUTHORIZATION)String token){
        if(!publications.existsById(id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Results have not been published");
        var election=elections.election(id,token);
        var calculated=ResultCalculator.calculate(election.candidates().stream().map(c->new ResultCalculator.CandidateName(c.id(),c.name())).toList(),elections.tally(id));
        return new ResultView(id,election.title(),calculated.totalVotes(),calculated.candidates(),calculated.winners(),calculated.tied());
    }
    @PostMapping("/{id}/publish") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Map<String,String> publish(@PathVariable @NonNull Long id,Authentication authentication,@RequestHeader(HttpHeaders.AUTHORIZATION)String token){
        var election=elections.election(id,token);
        if(!"CLOSED".equals(election.status()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Results can only be published after the election closes");
        Long adminId;try{adminId=Long.parseLong(authentication.getName());}catch(NumberFormatException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid administrator identity",ex);}
        publications.findById(id).orElseGet(()->publications.save(new ResultPublication(id,adminId)));
        return Map.of("message","Election results published");
    }
}
