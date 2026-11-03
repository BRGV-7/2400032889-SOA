package com.civicvote.election;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
@SpringBootApplication(exclude=UserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
public class ElectionApplication {
    public static void main(String[] args) { SpringApplication.run(ElectionApplication.class, args); }
}
