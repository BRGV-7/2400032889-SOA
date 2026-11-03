package com.civicvote.result;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Configuration
class ResultClientConfiguration {
    @Bean @LoadBalanced RestClient.Builder resultRestClientBuilder(){return RestClient.builder();}
}
@Component
class ElectionDataClient {
    private final RestClient client; private final String internalKey;
    ElectionDataClient(RestClient.Builder builder,@Value("${security.internal-key}")String internalKey){this.client=builder.baseUrl("http://ELECTION-SERVICE").build();this.internalKey=internalKey;}
    record CandidateResponse(Long id,String name){}
    record ElectionResponse(Long id,String title,String status,List<CandidateResponse> candidates){}
    ElectionResponse election(Long id,String token){
        try{return client.get().uri("/api/elections/{id}",id).header("Authorization",token).retrieve().body(ElectionResponse.class);}
        catch(org.springframework.web.client.RestClientException ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Unable to load election details",ex);}
    }
    Map<Long,Long> tally(Long electionId){
        try{
            @SuppressWarnings("unchecked") Map<String,Number> data=client.get().uri("http://VOTING-SERVICE/api/votes/internal/{id}/tally",electionId).header("X-Internal-Key",internalKey).retrieve().body(Map.class);
            if(data==null)return Map.of();
            return data.entrySet().stream().collect(java.util.stream.Collectors.toMap(entry->Long.parseLong(entry.getKey()),entry->entry.getValue().longValue()));
        }catch(org.springframework.web.client.RestClientException ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Unable to calculate persisted vote tally",ex);}
    }
}
