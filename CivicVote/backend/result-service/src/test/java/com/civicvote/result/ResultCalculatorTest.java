package com.civicvote.result;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
class ResultCalculatorTest {
    private final List<ResultCalculator.CandidateName> candidates=List.of(new ResultCalculator.CandidateName(1L,"A"),new ResultCalculator.CandidateName(2L,"B"));
    @Test void countsPercentagesAndTieAreCalculatedFromBallots(){
        var result=ResultCalculator.calculate(candidates,Map.of(1L,3L,2L,3L));
        assertEquals(6,result.totalVotes());assertEquals(List.of(1L,2L),result.winners());assertTrue(result.tied());assertEquals("50.00",result.candidates().get(0).percentage().toPlainString());
    }
    @Test void candidatesWithNoVotesHaveZeroPercentAndNoWinner(){
        var result=ResultCalculator.calculate(candidates,Map.of());
        assertEquals(0,result.totalVotes());assertTrue(result.winners().isEmpty());assertFalse(result.tied());assertEquals("0",result.candidates().get(0).percentage().toPlainString());
    }
    @Test void notaVotesAreIncludedInThePublishedTally(){
        var choices=List.of(new ResultCalculator.CandidateName(1L,"A"),new ResultCalculator.CandidateName(-1L,"NOTA (None of the Above)"));
        var result=ResultCalculator.calculate(choices,Map.of(1L,2L,-1L,3L));
        assertEquals(5,result.totalVotes());
        assertEquals(3,result.candidates().get(1).votes());
        assertEquals(List.of(-1L),result.winners());
    }
}
