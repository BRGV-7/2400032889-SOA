package com.civicvote.gateway;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.gateway.discovery.locator.enabled=false"
})
class GatewayRoutingTest {
    @Autowired RouteLocator routes;
    @Test void gatewayHasLoadBalancedRoutesForEveryBackendApi(){
        Set<String> ids=routes.getRoutes().map(route->route.getId()).collectList().block().stream().collect(Collectors.toSet());
        assertTrue(ids.containsAll(Set.of("auth-service","election-service","voting-service","result-service")));
    }
}
