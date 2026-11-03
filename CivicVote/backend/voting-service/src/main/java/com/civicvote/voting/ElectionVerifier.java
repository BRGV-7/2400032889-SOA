package com.civicvote.voting;
public interface ElectionVerifier {
    ElectionInfo verify(Long electionId,Long candidateId,String bearerToken);
    record ElectionInfo(String status,boolean candidateMember){}
}
