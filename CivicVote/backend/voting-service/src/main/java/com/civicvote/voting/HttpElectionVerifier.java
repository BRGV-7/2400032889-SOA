package com.civicvote.voting;
import java.util.List;
import java.time.Duration;
import java.net.http.HttpClient;
import java.util.Objects;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Configuration
class ElectionClientConfiguration {
    @Bean @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder(){
        var httpClient=Objects.requireNonNull(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
        var requestFactory=new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Objects.requireNonNull(Duration.ofSeconds(3)));
        return RestClient.builder().requestFactory(requestFactory);
    }
}
@Component
class HttpElectionVerifier implements ElectionVerifier {
    private final RestClient client;
    HttpElectionVerifier(RestClient.Builder builder){client=builder.baseUrl("http://ELECTION-SERVICE").build();}
    record CandidateInfo(Long id){}
    record ElectionResponse(String status,List<CandidateInfo> candidates){}
    @Override public ElectionInfo verify(Long electionId,Long candidateId,String bearerToken){
        try {
            ElectionResponse election=client.get().uri("/api/elections/{id}",electionId).header("Authorization",bearerToken).retrieve().body(ElectionResponse.class);
            if(election==null)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Election Service returned an empty response");
            boolean member=election.candidates()!=null&&election.candidates().stream().anyMatch(c->candidateId.equals(c.id()));
            return new ElectionInfo(election.status(),member);
        } catch(org.springframework.web.client.RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Unable to validate the election at this time",ex);
        }
    }
}
