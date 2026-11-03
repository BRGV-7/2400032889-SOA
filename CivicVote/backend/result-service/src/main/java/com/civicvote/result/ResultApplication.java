package com.civicvote.result;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
@SpringBootApplication(exclude=UserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
public class ResultApplication {
    public static void main(String[] args) { SpringApplication.run(ResultApplication.class, args); }
}
