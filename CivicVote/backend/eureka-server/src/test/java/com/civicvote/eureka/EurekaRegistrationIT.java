package com.civicvote.eureka;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.web.client.RestClient;
@EnabledIfSystemProperty(named="civicvote.eureka.integration",matches="true")
class EurekaRegistrationIT {
    @Test void dashboardListsAllBackendServices(){
        String registry=System.getProperty("civicvote.eureka.url","http://localhost:8761/eureka/apps");
        String response=RestClient.builder().build().get().uri(registry).header("Accept","application/xml").retrieve().body(String.class);
        assertNotNull(response);
        for(String app:new String[]{"API-GATEWAY","AUTH-SERVICE","ELECTION-SERVICE","VOTING-SERVICE","RESULT-SERVICE"})
            assertTrue(response.contains(app),"Expected registered service: "+app);
        assertTimeoutPreemptively(Duration.ofSeconds(1),()->assertFalse(response.contains("<status>DOWN</status>")));
    }
}
