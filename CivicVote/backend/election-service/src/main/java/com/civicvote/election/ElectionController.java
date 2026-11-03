package com.civicvote.election;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/elections")
public class ElectionController {
    private static final long NOTA_CANDIDATE_ID = -1L;
    private static final CandidateView NOTA_OPTION = new CandidateView(
        NOTA_CANDIDATE_ID, "NOTA (None of the Above)", "System option", "Reject all listed candidates");
    private final ElectionRepository elections;
    public ElectionController(ElectionRepository elections){this.elections=elections;}
    public record ElectionInput(@NotBlank @Size(max=160) String title,@Size(max=2000) String description,@NotNull Instant startsAt,@NotNull Instant endsAt){}
    public record CandidateInput(@NotBlank @Size(max=120) String name,@Size(max=120) String party,@Size(max=2000) String manifesto){}
    public record CandidateView(Long id,String name,String party,String manifesto){}
    public record ElectionView(Long id,String title,String description,Instant startsAt,Instant endsAt,String status,List<CandidateView> candidates){}
    private ElectionView view(Election e){List<CandidateView> candidates=new java.util.ArrayList<>(e.getCandidates().stream().map(c->new CandidateView(c.getId(),c.getName(),c.getParty(),c.getManifesto())).toList());candidates.add(NOTA_OPTION);return new ElectionView(e.getId(),e.getTitle(),e.getDescription(),e.getStartsAt(),e.getEndsAt(),e.getStatus(),List.copyOf(candidates));}
    @GetMapping @Transactional(readOnly=true) public List<ElectionView> list(){return elections.findAll().stream().map(this::view).toList();}
    @GetMapping("/{id}") @Transactional(readOnly=true) public ElectionView get(@PathVariable @NonNull Long id){return view(find(id));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ElectionView create(@Valid @RequestBody ElectionInput input){validateDates(input);return view(elections.save(new Election(input.title().trim(),input.description(),input.startsAt(),input.endsAt())));}
    @PutMapping("/{id}") @Transactional public ElectionView update(@PathVariable @NonNull Long id,@Valid @RequestBody ElectionInput input){Election e=find(id);if(!"SCHEDULED".equals(e.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only scheduled elections can be edited");validateDates(input);e.update(input.title().trim(),input.description(),input.startsAt(),input.endsAt());return view(e);}
    @PostMapping("/{id}/candidates") @ResponseStatus(HttpStatus.CREATED) @Transactional public ElectionView addCandidate(@PathVariable @NonNull Long id,@Valid @RequestBody CandidateInput input){Election e=find(id);if(!"SCHEDULED".equals(e.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Candidates can only be added before voting opens");if(isNotaName(input.name()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"NOTA is a reserved system option");e.getCandidates().add(new Candidate(e,input.name().trim(),input.party(),input.manifesto()));return view(elections.save(e));}
    @DeleteMapping("/{id}/candidates/{candidateId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional public void removeCandidate(@PathVariable @NonNull Long id,@PathVariable @NonNull Long candidateId){Election e=find(id);if(!"SCHEDULED".equals(e.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Candidates can only be removed before voting opens");boolean removed=e.getCandidates().removeIf(candidate->candidateId.equals(candidate.getId()));if(!removed)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Candidate not found");}
    private Election find(@NonNull Long id){return elections.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Election not found"));}
    private boolean isNotaName(String name){String normalized=name.replaceAll("[^A-Za-z0-9]","");return normalized.equalsIgnoreCase("NOTA")||normalized.equalsIgnoreCase("NOTANONEOFTHEABOVE")||normalized.equalsIgnoreCase("NONEOFTHEABOVE");}
    private void validateDates(ElectionInput input){if(!input.endsAt().isAfter(input.startsAt()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Election end must be later than its start");if(input.startsAt().isBefore(Instant.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Election start must be in the future");}
}
