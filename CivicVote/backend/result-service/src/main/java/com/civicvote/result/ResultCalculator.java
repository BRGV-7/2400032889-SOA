package com.civicvote.result;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
public final class ResultCalculator {
    private ResultCalculator(){}
    public record CandidateTally(Long candidateId,String candidateName,long votes,BigDecimal percentage){}
    public record Tally(long totalVotes,List<CandidateTally> candidates,List<Long> winners,boolean tied){}
    public static Tally calculate(List<CandidateName> candidates,java.util.Map<Long,Long> counts){
        long total=0;
        for(Long count:counts.values())total+=Objects.requireNonNull(count,"Vote counts must not be null");
        List<CandidateTally> rows=new ArrayList<>(candidates.size());
        for(CandidateName candidate:candidates){
            Objects.requireNonNull(candidate,"Candidates must not contain null");
            Long candidateId=Objects.requireNonNull(candidate.id(),"Candidate IDs must not be null");
            long votes=Objects.requireNonNull(counts.getOrDefault(candidateId,0L),"Vote counts must not be null");
            BigDecimal percent=total==0?BigDecimal.ZERO:BigDecimal.valueOf(votes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total),2,RoundingMode.HALF_UP);
            rows.add(new CandidateTally(candidateId,candidate.name(),votes,percent));
        }
        long max=0;
        for(CandidateTally row:rows)max=Math.max(max,row.votes());
        List<Long> winners=new ArrayList<>();
        if(max>0)for(CandidateTally row:rows)if(row.votes()==max)winners.add(row.candidateId());
        return new Tally(total,List.copyOf(rows),List.copyOf(winners),winners.size()>1);
    }
    public record CandidateName(Long id,String name){}
}
